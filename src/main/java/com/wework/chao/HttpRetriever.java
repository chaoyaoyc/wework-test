package com.wework.chao;

import java.io.IOException;

/**
 * Interface to get web content
 */
public interface HttpRetriever {
    /**
     * @param url http url
     * @return the web content of 'url'
     * @throws IOException
     */
    String get(String url) throws IOException;
}
