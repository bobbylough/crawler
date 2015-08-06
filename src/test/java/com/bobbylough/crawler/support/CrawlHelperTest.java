package com.bobbylough.crawler.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.bobbylough.crawler.model.Link;

public class CrawlHelperTest {

	private CrawlHelper helper;

	@Before
	public void setUp() throws Exception {
		helper = new CrawlHelper();
	}

	@Test
	public void testGetLinksEmpty() {
		List<String> results = helper.getLinks("", null);
		assertTrue(results.isEmpty());
	}

	/**
	 * find no links in string content
	 */
	@Test
	public void testGetLinksNoLinks() {
		List<String> results = helper.getLinks("Robert'); DROP TABLE Students;--", null);
		assertTrue(results.isEmpty());
	}

	/**
	 * find link within mixed case string where href is not the first attribute
	 */
	@Test
	public void testGetLinksOtherProperties() {
		List<String> results = helper.getLinks("drop tables; <A target=\"self\" href=\"test.com\"> junk junk",
				"http://www.bobbylough.com");
		assertEquals(1, results.size());
		assertEquals("test.com", results.get(0));
	}

	/**
	 * find link when anchor tag is upper case
	 */
	@Test
	public void testGetLinksUpperCaseLink() {
		List<String> results = helper.getLinks("drop tables; <A HREF=\"test.com\"> junk junk", null);
		assertEquals(1, results.size());
		assertEquals("test.com", results.get(0));
	}

	/**
	 * relative link found, append to content page path
	 */
	@Test
	public void testGetLinksRelativeLink() {
		List<String> results = helper
				.getLinks("drop tables; <A HREF=\"/test\"> junk junk", "http://www.bobbylough.com");
		assertEquals(1, results.size());
		assertEquals("http://www.bobbylough.com/test", results.get(0));
	}

	/**
	 * multiple links found in string content
	 */
	@Test
	public void testGetLinksMultipleLinks() {
		List<String> results = helper.getLinks(
				"drop tables; <A HREF=\"test.com\"> <a href=\"nexttest.org\". junk junk", null);
		assertEquals(2, results.size());
		assertEquals("test.com", results.get(0));
		assertEquals("nexttest.org", results.get(1));
	}

	/**
	 * parse out base doman from standard url
	 */
	@Test
	public void testGetBaseUrl() {
		String result = helper.getBaseUrl("http://www.bobbylough.com/test/index.html");
		assertEquals("http://www.bobbylough.com/test", result);
	}

	/**
	 * parse out base doman from url to a path
	 */
	@Test
	public void testGetBaseUrlDir() {
		String result = helper.getBaseUrl("http://www.bobbylough.com/test/");
		assertEquals("http://www.bobbylough.com/test", result);
	}

	/**
	 * parse out base doman from url which is just a domain
	 */
	@Test
	public void testGetBaseUrlDomain() {
		String result = helper.getBaseUrl("http://www.bobbylough.com");
		assertEquals("http://www.bobbylough.com", result);
	}

	/**
	 * Read an empty input stream
	 * 
	 * expected: return an empty String buffer
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetPageContentsEmpty() throws IOException {
		BufferedInputStream mockInput = mock(BufferedInputStream.class);
		when(mockInput.read(any(byte[].class))).thenReturn(-1);
		StringBuffer result = helper.getPageContents(mockInput);
		assertEquals(0, result.length());
		verify(mockInput, times(1)).read(any(byte[].class));
	}

	/**
	 * Read an input stream using a file over the maximum size
	 * 
	 * expected: return a string buffer including the first chunk of characters
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetPageContents() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream input = classLoader.getResourceAsStream("com/bobbylough/crawler/support/allV.txt");
		BufferedInputStream bufferedInput = new BufferedInputStream(input);
		StringBuffer result = helper.getPageContents(bufferedInput);
		assertEquals(50176, result.length());
		String filteredResult = result.toString().replace("v", "");
		assertEquals(148, filteredResult.length());
		filteredResult = filteredResult.replace("\n", "");
		assertEquals(0, filteredResult.length());
	}

	/**
	 * create link using an invalid url
	 */
	@Test
	public void testCreateLinkMalformed() {
		Link result = helper.createLink("htp:/bad.com");
		assertEquals("htp:/bad.com", result.getUrl());
		assertFalse(result.isValid());
	}

	/**
	 * create link using an valid url
	 */
	@Test
	public void testCreateLinkValid() {
		Link result = helper.createLink("http://good.com");
		assertEquals("http://good.com", result.getUrl());
		assertTrue(result.isValid());
	}

	/**
	 * create link using an valid url trim whitespace
	 */
	@Test
	public void testCreateLinkValidWhitespace() {
		Link result = helper.createLink("http://good.com   ");
		assertEquals("http://good.com", result.getUrl());
		assertTrue(result.isValid());
	}

	/**
	 * create link using an valid url trim whitespace
	 */
	@Test
	public void testCreateLinkNullUrl() {
		Link result = helper.createLink(null);
		assertEquals(null, result.getUrl());
		assertFalse(result.isValid());
	}
}
