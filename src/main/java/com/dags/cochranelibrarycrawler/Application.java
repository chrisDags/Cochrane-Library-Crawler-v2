package com.dags.cochranelibrarycrawler;

import com.dags.cochranelibrarycrawler.managers.Manager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {

    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {

        long startTime = System.nanoTime();

        //Starts scraping the front page of Cochrane Library's website to gather links on every topic
        FrontPageScraper frontPageScraper = new FrontPageScraper();
        frontPageScraper.scrapeFrontPage();

        // Begins thread management, executes thread Jobs
        Manager manager = new Manager(frontPageScraper.getFrontPageTopicUrlsList());
        manager.manageThreads();

        long endTime = System.nanoTime();
        long timeElapsed = startTime - endTime;

        /*
           It takes me around 5 - 8 mins for crawling to finish,
           this is dependent on internet speed.
        */

        logger.info("TIME ELAPSED: (in seconds) " + ((double) timeElapsed / 1_000_000_000.0));
    }
}