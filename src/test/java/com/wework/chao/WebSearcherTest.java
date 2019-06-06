package com.wework.chao;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class WebSearcherTest {
    @Mock
    private HttpRetriever httpRetriever;

    @Before
    public void setup() {
        initMocks();
    }

    private void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testContains() {
        final String URL = "google.com";
        String[] TEST_DATE = new String[] {
                "I Googled this", "true",
                "google", "false",
                "We google  this paper", "true"
        };
        WebSearcher searcher = new WebSearcher("google.* this", httpRetriever);
        for (int i = 0; i < TEST_DATE.length / 2; ++i) {
            try {
                when(httpRetriever.get(URL)).thenReturn(TEST_DATE[i * 2]);
                assertEquals(Boolean.parseBoolean(TEST_DATE[i * 2 + 1]), searcher.contains(URL));
            } catch (IOException e) {
                fail();
            }
        }
    }
}