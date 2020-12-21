package com.dags.cochranelibrarycrawler;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;


public class WriteToFile {
    /*
        Modify path
     */

    private static final Logger logger = LogManager.getLogger(WriteToFile.class);

    private final String path = "C:\\results\\cochrane_reviews.txt";
    private List<String> reviewList;

    public WriteToFile(List<String> reviewList) {
        this.reviewList = reviewList;
    }

    public void outputToFile() {
        File file = new File(path);

        // Sorts each String by title in alphabetical order
        reviewList.sort(Comparator.comparing(s -> s.substring(StringUtils.ordinalIndexOf(s, "|", 1),
                StringUtils.ordinalIndexOf(s, "|", 2))));

        //Trims extra newline character of the last String
        reviewList.set(reviewList.size() - 1, reviewList.get(reviewList.size() - 1).trim());

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            // As of 8/12/2020 , 31009 lines are written to file.
            for (String elem : reviewList) {
                bufferedWriter.write(elem);
            }

            bufferedWriter.close();
            logger.info("\nFile created at: " + path);
        } catch (IOException e) {
            logger.error(e);
        }

    }
}
