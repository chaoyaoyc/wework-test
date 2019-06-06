package com.wework.chao;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Supports searching a regex in the web content.
 */
public class WebSearcher {
    private final Pattern pattern;
    private final HttpRetriever httpRetriever;

    /**
     * Creates with regex and the default HttpRetriever
     * @param pattern regex pattern
     */
    public WebSearcher(String pattern) {
        this(pattern, new ApacheHttpRetriever());
    }

    /**
     * @param pattern regex pattern
     * @param httpRetriever HttpRetriever object that gets web contents into a string
     */
    public WebSearcher(String pattern, HttpRetriever httpRetriever) {
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        this.httpRetriever = httpRetriever;
    }

    /**
     * @param url a web url link
     * @return whether the web page specified by 'url' contains the regex.
     * @throws IOException
     */
    public boolean contains(String url) throws IOException {
        String content = httpRetriever.get(url);
        return pattern.matcher(content).find();
    }
}
