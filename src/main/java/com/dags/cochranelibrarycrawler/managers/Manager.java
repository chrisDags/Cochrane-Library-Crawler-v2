package com.dags.cochranelibrarycrawler.managers;

import com.dags.cochranelibrarycrawler.WriteToFile;
import com.dags.cochranelibrarycrawler.sharedresources.ResultsListResource;
import com.dags.cochranelibrarycrawler.workers.Worker;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
    Manages the creation of threads
 */

public class Manager {

    private static final Logger logger = LogManager.getLogger(Manager.class);

    private List<String> frontPageTopicUrls;

    public Manager(List<String> frontPageTopicUrls) {
        this.frontPageTopicUrls = frontPageTopicUrls;
    }


    public void manageThreads() {

        logger.info("\n\n- THREAD MANAGER STARTED -\n\n");


        List<Runnable> threadList = new ArrayList<>();

        /*
            Circular redirect must be allowed.
        */
        RequestConfig requestConfig = RequestConfig.custom()
                .setCircularRedirectsAllowed(true)
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();

        /*
            PoolingHttpConnectionManager
         */

        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(frontPageTopicUrls.size());
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(frontPageTopicUrls.size());

        ExecutorService executorService = Executors.newFixedThreadPool(frontPageTopicUrls.size());


        for (String elem : frontPageTopicUrls) {
            threadList.add(new Worker(poolingHttpClientConnectionManager, requestConfig, elem));
        }

        for (Runnable runnable : threadList) {
            executorService.submit(runnable);
        }

        executorService.shutdown();

        try {
            // Waits 35 mins for executorService to finish.
            if (!executorService.awaitTermination(35L, TimeUnit.MINUTES)) {
                executorService.shutdown();
            }
        } catch (InterruptedException e) {
            executorService.shutdown();
            Thread.currentThread().interrupt();
        }

        logger.info("Crawling and scraping complete!");
        logger.info("Writing reviewResultsList to file...");

        // Starts file creation
        WriteToFile writeToFile = new WriteToFile(ResultsListResource.reviewResultsList);
        writeToFile.outputToFile();

    }
}