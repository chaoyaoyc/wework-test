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

    @Test
    public void testIgnoreCertificate() throws Exception {
        assertTrue(retriever.get("webeden.co.uk").contains("twitter"));
    }

    @Test
    public void testWWW() throws Exception {
        assertTrue(retriever.get("fda.gov").contains("fda"));
    }
}