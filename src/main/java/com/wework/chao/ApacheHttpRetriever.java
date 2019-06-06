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
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Uses org.apache.httpcomponents to retrieve the web page.
 */
public class ApacheHttpRetriever implements HttpRetriever {
    private static final String HTTP_PREFIX = "http://";
    private static final String WWW_PREFIX = "www.";

    private static CloseableHttpClient getCloseableHttpClient() throws IOException {
        CloseableHttpClient httpClient = null;
        try {
            // Ignores certificate error, set redirect, and set cookie
            httpClient = HttpClients.custom().
                    setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).
                    setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, (x509Certificates, s) -> true).build())
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setCookieSpec(CookieSpecs.STANDARD).build())
                    .build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IOException(e);
        }
        return httpClient;
    }

    /**
     * Redirect if necessary, for example "http://twitter.com" -> "https://twitter.com"
     * @param url  starting with "http://"
     * @return url or redirected location
     * @throws IOException
     * @throws URISyntaxException
     */
    private URI getLocation(String url) throws IOException {
        CloseableHttpClient httpClient = getCloseableHttpClient();
        try {
            HttpClientContext context = HttpClientContext.create();
            HttpGet httpGet = new HttpGet(url);

            httpClient.execute(httpGet, context);
            HttpHost target = context.getTargetHost();
            List<URI> redirectLocations = context.getRedirectLocations();
            URI location = URIUtils.resolve(httpGet.getURI(), target, redirectLocations);
            return location;
        } catch (URISyntaxException e) {
            throw new IOException(e);
        } finally {
            httpClient.close();
        }
    }

    /**
     * @param url http url
     * @return the web content of 'url'
     * @throws IOException
     */
    public String get(String url) throws IOException {
        String httpUrl = url;
        if (!url.startsWith(HTTP_PREFIX)) {
            httpUrl = HTTP_PREFIX + httpUrl;
        }

        URI location = null;
        try {
            location = getLocation(httpUrl);
        } catch (UnknownHostException e) {
            // try again with "www."
            httpUrl = HTTP_PREFIX + WWW_PREFIX + url;
            location = getLocation(httpUrl);
        }
        HttpClient client = getCloseableHttpClient();
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
