package com.wework.chao;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ApacheHttpRetrieverTest {
    private ApacheHttpRetriever retriever;

    @Before
    public void setup() {
        retriever = new ApacheHttpRetriever();
    }

    @Test
    public void testGetNoDirect() throws Exception {
        String content = retriever.get("google.com");
        assertTrue(content.contains("google"));
    }

    @Test
    public void testGetDirect() throws Exception {
        String content = retriever.get("twitter.com");
        assertTrue(content.contains("twitter"));
    }
}