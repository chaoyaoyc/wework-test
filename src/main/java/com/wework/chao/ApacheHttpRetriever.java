package com.wework.chao;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 *
 */
public class ApacheHttpRetriever implements HttpRetriever {
    private static final String HTTP_PREFIX = "http://";

    /**
     * Redirect if necessary, for example "http://twitter.com" -> "https://twitter.com"
     * @param url  starting with "http://"
     * @return url or redirected location
     * @throws IOException
     * @throws URISyntaxException
     */
    private URI getLocation(String url) throws IOException, URISyntaxException {
        CloseableHttpClient httpclient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        try {
            HttpClientContext context = HttpClientContext.create();
            HttpGet httpGet = new HttpGet(url);

            httpclient.execute(httpGet, context);
            HttpHost target = context.getTargetHost();
            List<URI> redirectLocations = context.getRedirectLocations();
            URI location = URIUtils.resolve(httpGet.getURI(), target, redirectLocations);
            return location;
        } finally {
            httpclient.close();
        }
    }

    /**
     * @param url http url
     * @return the web content of 'url'
     * @throws IOException
     */
    public String get(String url) throws IOException {
        if (!url.startsWith("HTTP_PREFIX")) {
            url = HTTP_PREFIX + url;
        }

        URI location = null;
        try {
            location = getLocation(url);
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage());
        }
        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        try {
            HttpGet request = new HttpGet(location);
            request.addHeader("User-Agent", "Apache HTTPClient");
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity);
        } finally {
        }
    }
}
