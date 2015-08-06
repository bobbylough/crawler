package com.bobbylough.crawler;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.bobbylough.crawler.model.Link;
import com.bobbylough.crawler.support.CrawlHelper;

public class WebCrawlerTest {

	private WebCrawler webCrawler;
	private CrawlHelper helper;
	private HttpClient client;
	private ByteArrayOutputStream outContent;

	@Before
	public void setUp() throws Exception {
		helper = mock(CrawlHelper.class);
		client = mock(HttpClient.class);
		outContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent));
		webCrawler = new WebCrawler(helper, client);
	}

	@After
	public void cleanUp() {
		System.setOut(null);
	}

	/**
	 * Custom Matcher for Http Get Objects
	 *
	 */
	private class IsHttpGetUrl extends ArgumentMatcher<HttpGet> {
		private String url;

		public IsHttpGetUrl(String url) {
			this.url = url;
		}

		@Override
		public boolean matches(Object httpGet) {
			return httpGet instanceof HttpGet && ((HttpGet) httpGet).getURI().toString().equals(url);
		}
	}

	/**
	 * validate the reporting of an invalid root url
	 * 
	 * expected: nothing crawled just one invalid url in the report
	 */
	@Test
	public void testCrawlMalformed() {
		Link badLink = new Link("bad");
		badLink.setValid(false);
		when(helper.createLink("bad")).thenReturn(badLink);

		webCrawler.crawl("bad");
		assertEquals("", outContent.toString());
		webCrawler.report();
		assertEquals("INVALID - bad\r\n", outContent.toString());

		verifyZeroInteractions(client);
	}

	/**
	 * validate the reporting of an valid root url without any links
	 * 
	 * expected: page crawled but only one url in the report
	 */
	@Test
	public void testCrawlNoLinks() throws ClientProtocolException, IOException {

		Link link = new Link("http://www.bobbylough.com");
		link.setValid(true);
		when(helper.createLink("http://www.bobbylough.com   ")).thenReturn(link);

		HttpResponse response = mock(HttpResponse.class);
		StatusLine statusLine = mock(StatusLine.class);
		HttpEntity entity = mock(HttpEntity.class);

		when(client.execute(argThat(new IsHttpGetUrl("http://www.bobbylough.com")))).thenReturn(response);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(200);
		when(response.getEntity()).thenReturn(entity);
		InputStream inputStream = mock(InputStream.class);
		when(entity.getContent()).thenReturn(inputStream);
		StringBuffer pageContent = new StringBuffer();
		when(helper.getPageContents(any(BufferedInputStream.class))).thenReturn(pageContent);
		when(helper.getLinks(pageContent.toString(), "http://www.bobbylough.com")).thenReturn(new ArrayList<String>());

		webCrawler.crawl("http://www.bobbylough.com   ");
		assertEquals("", outContent.toString());
		webCrawler.report();
		assertEquals("200 - http://www.bobbylough.com\r\n", outContent.toString());

		verify(inputStream, times(1)).close();
	}

	/**
	 * hit the maximum of links discovered, with mixed response statuses
	 * 
	 * expected: root page crawled and 49 other
	 */
	@Test
	public void testCrawlMaxLinks() throws ClientProtocolException, IOException {
		List<String> linkUrls = new ArrayList<String>();
		linkUrls.add("http://www.bobbylough.com");
		for (int i = 0; i <= 60; i++) {
			linkUrls.add("http://www.bobbylough.com/test/" + i);
		}
		when(helper.createLink(any(String.class))).thenAnswer(new Answer<Link>() {
			public Link answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return new Link((String) args[0]);
			}
		});

		HttpResponse goodResponse = mock(HttpResponse.class);
		HttpResponse badResponse = mock(HttpResponse.class);
		StatusLine goodStatusLine = mock(StatusLine.class);
		StatusLine badStatusLine = mock(StatusLine.class);
		HttpEntity entity = mock(HttpEntity.class);

		when(client.execute(argThat(new IsHttpGetUrl("http://www.bobbylough.com")))).thenReturn(goodResponse);
		when(client.execute(not(argThat(new IsHttpGetUrl("http://www.bobbylough.com"))))).thenReturn(badResponse);
		when(goodResponse.getStatusLine()).thenReturn(goodStatusLine);
		when(badResponse.getStatusLine()).thenReturn(badStatusLine);
		when(goodStatusLine.getStatusCode()).thenReturn(200);
		when(badStatusLine.getStatusCode()).thenReturn(501);
		when(goodResponse.getEntity()).thenReturn(entity);
		when(badResponse.getEntity()).thenReturn(entity);
		InputStream inputStream = mock(InputStream.class);
		when(entity.getContent()).thenReturn(inputStream);
		StringBuffer pageContent = new StringBuffer();
		when(helper.getPageContents(any(BufferedInputStream.class))).thenReturn(pageContent);
		when(helper.getLinks(pageContent.toString(), "http://www.bobbylough.com")).thenReturn(linkUrls);

		webCrawler.crawl("http://www.bobbylough.com");
		assertEquals("", outContent.toString());
		webCrawler.report();
		String[] reportLines = outContent.toString().split("\r\n");
		assertEquals(50, reportLines.length);
		assertEquals("200 - http://www.bobbylough.com", reportLines[0]);
		assertEquals("501 - http://www.bobbylough.com/test/0", reportLines[1]);
		for (int i = 2; i < 50; i++) {
			assertEquals("501 - http://www.bobbylough.com/test/" + (i - 1), reportLines[i]);
		}

		verify(inputStream, times(50)).close();
	}

	/**
	 * follow link encounters ClientProtocolException
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	@Test
	public void testCrawlClientProtocolException() throws ClientProtocolException, IOException {

		Link badLink = new Link("badGet.com");
		badLink.setValid(true);
		when(helper.createLink("badGet.com")).thenReturn(badLink);

		when(client.execute(any(HttpGet.class))).thenThrow(mock(ClientProtocolException.class));

		webCrawler.crawl("badGet.com");
		assertEquals("", outContent.toString());
		webCrawler.report();
		assertEquals("INVALID - badGet.com\r\n", outContent.toString());

		verify(helper).createLink(any(String.class));
		verifyNoMoreInteractions(helper);
	}

	/**
	 * follow link encounters IOException
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	@Test
	public void testCrawlIOException() throws ClientProtocolException, IOException {

		Link badLink = new Link("badGet.com");
		badLink.setValid(true);
		when(helper.createLink("badGet.com")).thenReturn(badLink);

		when(client.execute(any(HttpGet.class))).thenThrow(mock(IOException.class));

		webCrawler.crawl("badGet.com");
		assertEquals("", outContent.toString());
		webCrawler.report();
		assertEquals("INVALID - badGet.com\r\n", outContent.toString());

		verify(helper).createLink(any(String.class));
		verifyNoMoreInteractions(helper);
	}

	/**
	 * follow link encounters ConnectionPoolTimeoutException
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	@Test
	public void testCrawlConnectionPoolTimeoutException() throws ClientProtocolException, IOException {

		Link badLink = new Link("badGet.com");
		badLink.setValid(true);
		when(helper.createLink("badGet.com")).thenReturn(badLink);

		when(client.execute(any(HttpGet.class))).thenThrow(mock(ConnectionPoolTimeoutException.class));

		webCrawler.crawl("badGet.com");
		assertEquals("", outContent.toString());
		webCrawler.report();
		assertEquals("404 - badGet.com\r\n", outContent.toString());

		verify(helper).createLink(any(String.class));
		verifyNoMoreInteractions(helper);
	}

	/**
	 * follow link encounters ConnectionPoolTimeoutException
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	@Test
	public void testCrawlException() throws ClientProtocolException, IOException {

		Link badLink = new Link("badGet.com");
		badLink.setValid(true);
		when(helper.createLink("badGet.com")).thenReturn(badLink);

		when(client.execute(any(HttpGet.class))).thenThrow(mock(NullPointerException.class));

		webCrawler.crawl("badGet.com");
		assertEquals("", outContent.toString());
		webCrawler.report();
		assertEquals("INVALID - badGet.com\r\n", outContent.toString());

		verify(helper).createLink(any(String.class));
		verifyNoMoreInteractions(helper);
	}

	/**
	 * follow link encounters IllegalStateException
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	@Test
	public void testCrawlIllegalStateException() throws ClientProtocolException, IOException {

		Link badLink = new Link("badGet.com");
		badLink.setValid(true);
		when(helper.createLink("badGet.com")).thenReturn(badLink);

		when(client.execute(any(HttpGet.class))).thenThrow(mock(IllegalStateException.class));

		webCrawler.crawl("badGet.com");
		assertEquals("", outContent.toString());
		webCrawler.report();
		assertEquals("INVALID - badGet.com\r\n", outContent.toString());

		verify(helper).createLink(any(String.class));
		verifyNoMoreInteractions(helper);
	}
}
