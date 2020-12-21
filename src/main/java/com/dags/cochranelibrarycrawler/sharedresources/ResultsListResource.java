package com.dags.cochranelibrarycrawler.sharedresources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultsListResource {
    public static List<String> reviewResultsList = Collections.synchronizedList(new ArrayList<>());
}
