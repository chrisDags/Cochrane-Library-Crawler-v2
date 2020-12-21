package com.dags.cochranelibrarycrawler.jobs;

import com.dags.cochranelibrarycrawler.sharedresources.ResultsListResource;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
    Crawls through each page of each topic after visiting the topic link
 */

public class Job {

    private static final Logger logger = LogManager.getLogger(Job.class);

    private String baseUri = "https://www.cochranelibrary.com";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private int page;
    private int lineCount;
    private String globalTopicName = "";
    private StringBuilder reviewsStringBuilder = new StringBuilder();
    private List<Integer> pageList = new ArrayList<>();

    public void crawl(RequestConfig requestConfig, CloseableHttpClient closeableHttpClient,
                      String url) throws IOException {

        logger.info("Thread has started scraping.");
        //String nextUrl = null;
        String nextUrl = url;
        HttpGet httpget;

        while (!nextUrl.isEmpty()) {

            page++;
            httpget = new HttpGet(nextUrl);
            httpget.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
            httpget.setConfig(requestConfig);
            CloseableHttpResponse closeableHttpResponse = null;

            try {
                closeableHttpResponse = closeableHttpClient.execute(httpget);

                ////System.out.println(closeableHttpResponse);

                int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    logger.error("Bad HTTP status code returned. Status code: " + statusCode);
                    throw new RuntimeException();
                }

                HttpEntity entity = closeableHttpResponse.getEntity();

                String entityStr = EntityUtils.toString(entity, "UTF-8");

                Document document = Jsoup.parse(entityStr, "UTF-8");

                /// System.out.println(document);

                // document = Jsoup.parse(closeableHttpResponse.toString());
                //System.out.println(document);

                // JSoup is used to query results from the document to Elements
                Elements authorsElement = document.select("div.search-result-authors");
                Elements titlesElement = document.select(".search-results-item .result-title");
                String topicStr = document.select(".results-count #searchResultText").text();
                Elements urlsElement = document.select(".result-title a[href]");
                Elements dateElement = document.select(".search-result-date");

                if (globalTopicName.isEmpty()) {
                    globalTopicName = topicStr;
                }

                // Handles connection errors related to page visitation. The loop will be restarted if the page didn't
                // load, or if a 500 status code occurs. The website itself is very fragile, so this situation is expected.
                if (topicStr.isEmpty()) {

                    logger.warn("\nThere was a connection issue on page #" + page
                            + " of the topic '" + globalTopicName + "' ...retrying");

                    pageList.add(page);
                    logger.info("Total connection errors on page: " + pageList.size());


                    /*
                       Attempts to retry the connection per page is limited to 10. Pages will typically
                       load properly after, at most and very rarely, 3 retries.
                      */

                    if (pageList.size() < 10) {
                        continue;
                    } else {
                        logger.info("\nPage failed to load after 10 attempts. Articles " +
                                "on " + "'" + globalTopicName + "'" + " Will be incomplete");
                        ResultsListResource.reviewResultsList.add(reviewsStringBuilder.toString());
                        return;
                    }
                }

                nextUrl = document.select(".search-results-footer .pagination div.pagination-next-link a[href^=\"https\"]").attr("href");

                int size = titlesElement.size();

                for (int i = 0; i < size; i++) {
                    lineCount++;
                    reviewsStringBuilder.append(baseUri)
                            .append(urlsElement.get(i).attr("href"))
                            .append("|")
                            .append(topicStr).append("|")
                            .append(titlesElement.get(i).text())
                            .append("|")
                            .append(authorsElement.get(i).text())
                            .append("|")
                            .append(simpleDateFormat.format(new Date(dateElement.get(i).text())))
                            .append("\n\n");
                }

                logger.info("Topic: " + topicStr);
                logger.info("Current Page: #" + page);
                logger.info("Next URL to visit: " + nextUrl + "\n");

                // Adds the StringBuilder to a thread-safe list when job is complete.
                if (nextUrl.isEmpty()) {
                    ResultsListResource.reviewResultsList.add(reviewsStringBuilder.toString());
                    logger.info("Topic: " + globalTopicName);
                    logger.info("Scraping successfully completed. \n - Total lines to write to file: "
                            + lineCount + "\n");
                }

                // Clears the pageList if there is another URL to visit
                pageList.clear();

            } catch (RuntimeException e) {
                logger.error(e);
            } finally {
                if (closeableHttpResponse != null) {
                    try {
                        closeableHttpResponse.close();
                    } catch (IOException e) {
                        logger.error(e);

                    }
                }
            }

        }
    }
}