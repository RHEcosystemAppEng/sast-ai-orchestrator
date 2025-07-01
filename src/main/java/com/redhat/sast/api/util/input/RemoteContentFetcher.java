package com.redhat.sast.api.util.input;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import jakarta.annotation.Nonnull;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RemoteContentFetcher {

    private static final Logger LOG = Logger.getLogger(RemoteContentFetcher.class);
    private static final String USER_AGENT = "SAST-AI-Orchestrator/1.0"; 
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(20))
            .build();


    /**
     * Takes any public URL and fetches its raw string content.
     *
     * @param sourceUrl The non-null, public URL to fetch content from.
     * @return Raw string content from the URL.
     * @throws IOException if the URL is invalid, fetching fails, or the response is not successful.
     * @throws InterruptedException if the operation is interrupted.
     */
    public String fetch(@Nonnull String sourceUrl) throws IOException, InterruptedException {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new IllegalArgumentException("Source URL cannot be null or empty.");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sourceUrl))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                handleHttpError(response, sourceUrl);
            }

            String content = response.body();

            LOG.infof("Successfully fetched content from %s. Length: %d characters", sourceUrl, content.length());
            return content;
        } catch (IllegalArgumentException e) {
            LOG.errorf(e, "Invalid URL syntax: %s", sourceUrl);
            throw new IOException("The provided URL is malformed: " + sourceUrl, e);
        } catch (HttpTimeoutException e) {
            LOG.errorf(e, "Request timed out for URL: %s", sourceUrl);
            throw new IOException("The request to the URL timed out: " + sourceUrl, e);
        } catch (IOException e) {
            LOG.errorf(e, "An I/O error occurred when fetching URL: %s", sourceUrl);
            throw e;
        }
    }

    private void handleHttpError(HttpResponse<String> response, String url) throws IOException {
        String errorMessage;
        switch (response.statusCode()) {
            case 401:
                errorMessage = "Access denied (401 Unauthorized).";
                break;
            case 403:
                errorMessage = "Access forbidden (403 Forbidden). Please ensure the URL is public.";
                break;
            case 404:
                errorMessage = "Not found (404). Please verify the URL is correct.";
                break;
            default:
                errorMessage = "Request failed with HTTP status code: " + response.statusCode();
                break;
        }
        throw new IOException(errorMessage + " URL: " + url);
    }
}
