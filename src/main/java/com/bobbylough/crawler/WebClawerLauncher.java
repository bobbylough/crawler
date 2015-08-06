package com.bobbylough.crawler;

/**
 * Basic Web Crawler - Crawls a page starting at a root url.
 * 
 * Arguments:
 * 
 * 1: (Required) root url (example: http://www.bobbylough.com)
 * 
 * 2 & 3: (Optional) proxy host & proxy port
 *
 */
public class WebClawerLauncher {

	/**
	 * Launch the web crawler
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1 || args.length == 3) {
			WebCrawler crawler;
			if (args.length == 3) {
				crawler = new WebCrawler(args[1], Integer.parseInt(args[2]));
			} else {
				crawler = new WebCrawler();
			}
			crawler.crawl(args[0]);
			crawler.report();
		} else {
			System.out.println("Invalid Arguments - A single url is required.");
		}
	}

}
