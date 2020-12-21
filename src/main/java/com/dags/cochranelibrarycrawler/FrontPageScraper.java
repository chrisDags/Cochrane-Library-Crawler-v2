package com.dags.cochranelibrarycrawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
    Scrapes the front page of Cochrane Library's website for links to each topic
 */

public class FrontPageScraper {

    private static final Logger logger = LogManager.getLogger(FrontPageScraper.class);

    private Document document;
    private List<String> frontPageTopicUrlsList = new ArrayList<>();

    public void scrapeFrontPage() {

        logger.info("Scraping front page . . .");

        String frontPageUrl = "https://www.cochranelibrary.com/";

        try {
            /*
                Timeout set for 15 seconds. The website is often times very slow, even on a browser,
                so this is a necessary inclusion.
             */
            document = Jsoup.connect(frontPageUrl)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .timeout(15_000)
                    .get();
        } catch (IOException e) {
            logger.fatal(e);
            System.exit(1);
        }

        // Gathers the links on the front page and retrieves those relating to topics
        Elements frontPageLinks = document.select(".browse-by-list-item a");
        for (Element elem : frontPageLinks) {
            if (!elem.attr("href").endsWith("-true")) {
                String link = elem.attr("href");
                frontPageTopicUrlsList.add(link);
            }
        }
    }

    // Getter for the list of URLs
    public List<String> getFrontPageTopicUrlsList() {
        return this.frontPageTopicUrlsList;
    }
}