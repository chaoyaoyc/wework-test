package com.wework.chao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Concurrently checks whether each web page contains the regex.
 */
public class WebSearchExecutor {
    private final WebSearcher searcher;
    private final BufferedReader input;  // input of URLs
    private final PrintWriter output;   // destination of results
    private final int maxConcurrentRequests;  // the max number of concurrent web requests
    private final BlockingQueue<String> queue; // blocking queue to store input lines
    private final AtomicBoolean done;

    private static final int MAX_CONCURRENT_REQUESTS = 20; // the default max number of concurrent web requests
    private static final int POLL_TIME_OUT = 50;  // Timeout to read the queue
    private static final int MAX_QUEUE_FACTOR = 5;  // factor determining the blocking queue capacity

    public WebSearchExecutor(WebSearcher searcher, InputStream input, OutputStream output) {
        this(searcher, input, output, MAX_CONCURRENT_REQUESTS);
    }

    public WebSearchExecutor(WebSearcher searcher, InputStream input, OutputStream output, int maxConcurrentRequests) {
        this.searcher = searcher;
        this.input = new BufferedReader(new InputStreamReader(input));
        this.output = new PrintWriter(new OutputStreamWriter(output), true);
        this.maxConcurrentRequests = maxConcurrentRequests;
        this.queue = new ArrayBlockingQueue<>(MAX_QUEUE_FACTOR * maxConcurrentRequests);
        this.done  = new AtomicBoolean(false);
    }

    /**
     * Reads each url, checks whether each web page contains url and writes the results into the output.
     * @throws IOException
     */
    public void run() throws IOException {
        Thread[] threads = startThreads();
        String line = input.readLine(); // ignore the header
        while ((line = input.readLine()) != null) {
            try {
                queue.put(line);
            } catch (InterruptedException e) {
                break;
            }
        }
        finishThreads(threads);
    }

    /**
     * Synchronized writes the result into the output.
     * @param url url string
     * @param hasTerm true, false, or the error message
     */
    private synchronized void writeResult(String url, String hasTerm) {
        System.out.println(url + "," + hasTerm); // logging
        output.println(url + "," + hasTerm);
    }

    /**
     * @param line a line from the input stream
     * @return url that extracted from the line
     */
    private Optional<String> getUrl(String line) {
        int pos1 = line.indexOf("\"");
        if (pos1 == -1) {
            return Optional.empty();
        }
        int pos2  = line.indexOf("\"", pos1 + 1);
        if (pos2 == -1) {
            return Optional.empty();
        }
        String url = line.substring(pos1 + 1, pos2).trim();
        return Optional.of(url);
    }

    /**
     * Signals all threads no more URLs
     */
    private void finishThreads(Thread[] threads) {
        done.set(true);
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * Starts all threads to read the queue and check each URL.
     */
    private Thread[] startThreads() {
        done.set(false);
        Thread[] threads = new Thread[maxConcurrentRequests];
        for (int i = 0; i < maxConcurrentRequests; ++i) {
            threads[i] = new Thread(() -> {
                while (true) {
                    String line = null;
                    try {
                        line = queue.poll(POLL_TIME_OUT, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        return;
                    }
                    if (line != null) {
                        searchOneUrl(line);
                    } else if (done.get()) {
                        return;
                    }
                }
            });
            threads[i].start();
        }
        return threads;
    }

    /**
     * Gets the web page, checks whether it contains the specified regex, and writes true or false with the url
     * into the output. If there is an error with connection, writes the error message
     * @param line a line from input containing url
     * @throws IOException
     */
    private void searchOneUrl(final String line) {
        Optional<String> url = getUrl(line);
        if (!url.isPresent()) {
            return;
        }
        boolean hasTerm = false;
        String msg = null;
        try {
            hasTerm = searcher.contains(url.get());
        } catch (IOException e) {
            msg = e.getMessage();
        }
        msg = msg != null ? msg : Boolean.toString(hasTerm);
        writeResult(url.get(), msg);
    }
}
