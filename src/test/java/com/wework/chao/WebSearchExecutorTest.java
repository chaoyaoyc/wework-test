package com.wework.chao;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class WebSearchExecutorTest {

    @Mock
    private WebSearcher webSearcher;
    private ByteArrayOutputStream outputStream;

    @Before
    public void setup() {
        initMocks();
        outputStream = new ByteArrayOutputStream(4000);
    }

    private void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRun() {
        final String INPUT_URLS = "header\n"
                + "0,\"google.com\",xxx\n"
                + "1,\"facebook.com\",xxx\n"
                + "2,\"twitter.com\",xxx\n"
                + "3,\"cnn.com\",xxx\n"
                + "4,\"stanford.edu\",xxx\n"
                + "5,\"error.com\",xxx\n";
        final String[] URLS = new String[] {
           "google.com","facebook.com","twitter.com","cnn.com","stanford.edu","error.com"
        };
        final boolean[] RESULT = new boolean[] {
           true, false, true, false, false
        };
        final String ERROR_MSG = "error";
        Map<String, String> results = new HashMap<>();
        AtomicInteger currentNumThreads = new AtomicInteger(0);
        AtomicInteger maxNumThreads = new AtomicInteger(0);

        Random random = new Random();
        // Mocks each url checking, record the maximum num of the current requests and return the results
        for (int i = 0; i < URLS.length; ++i) {
            if (i < URLS.length - 1) {
                results.put(URLS[i], Boolean.toString(RESULT[i]));
            } else {
                results.put(URLS[i], ERROR_MSG); // Test IOException
            }

            try {
                final int index = i;
                when(webSearcher.contains(URLS[index])).thenAnswer(url -> {
                    int num = currentNumThreads.incrementAndGet();
                    synchronized (maxNumThreads) {
                        if (num > maxNumThreads.get()) {
                            maxNumThreads.set(num);
                        }
                    }
                    Thread.sleep(100 + random.nextInt() % 100);
                    currentNumThreads.decrementAndGet();
                    if (index < URLS.length - 1) {
                        return RESULT[index];
                    } else {
                        throw new IOException(ERROR_MSG);
                    }
                });
            } catch (IOException e) {
                fail();
            }
        }

        // Checks the results
        InputStream inputStream = new ByteArrayInputStream(INPUT_URLS.getBytes());
        final int MAX_JOBS = 1;
        WebSearchExecutor executor = new WebSearchExecutor(webSearcher, inputStream, outputStream, MAX_JOBS);
        try {
            executor.run();
        } catch (IOException e) {
            fail();
        }
        String[] actualResult = new String(outputStream.toByteArray()).split("\n");
        assertEquals(URLS.length, actualResult.length);
        for (int i = 0; i < URLS.length; ++i) {
            String[] res = actualResult[i].split(",");
            assertEquals(res[1], results.get(res[0]));
        }
        assertEquals(MAX_JOBS, maxNumThreads.get());
    }

}