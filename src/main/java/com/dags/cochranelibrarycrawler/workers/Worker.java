package com.dags.cochranelibrarycrawler.workers;

import com.dags.cochranelibrarycrawler.jobs.Job;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/*
    Worker thread
 */

public class Worker implements Runnable {

    private static final Logger logger = LogManager.getLogger(Worker.class);
    private final RequestConfig requestConfig;
    private final CloseableHttpClient closeableHttpClient;

    private String url;

    public Worker(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager, RequestConfig requestConfig,
                  String url) {
        this.requestConfig = requestConfig;
        // A UserAgent must be set, and a laxRedirectStrategy must be used for automatic redirection.
        this.closeableHttpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36")
                .build();

        this.url = url;

    }

    @Override
    public void run() {
        Job job = new Job();
        try {
            job.crawl(requestConfig, closeableHttpClient, url);
        } catch (IOException e) {
            //LoggerResource.logger.warn(e);
            logger.warn(e);
        }
    }

}
