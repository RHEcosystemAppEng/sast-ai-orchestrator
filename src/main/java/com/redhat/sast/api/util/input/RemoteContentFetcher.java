package com.redhat.sast.api.util.input;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class RemoteContentFetcher {

    private static final String USER_AGENT = "SAST-AI-Orchestrator/1.0";
    private static final int MAX_RETRIES = 3;
    private static final int REQUEST_TIMEOUT_SECONDS = 5;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Takes any public URL and fetches its raw string content with retry mechanism.
     *
     * @param sourceUrl The non-null, public URL to fetch content from.
     * @return Raw string content from the URL.
     * @throws IOException if the URL is invalid, fetching fails permanently, or the response is not successful.
     * @throws InterruptedException if the operation is interrupted.
     */
    public String fetch(@Nonnull String sourceUrl) throws IOException, InterruptedException {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new IllegalArgumentException("Source URL cannot be null or empty.");
        }

        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return attemptFetch(sourceUrl, attempt);
            } catch (IOException e) {
                lastException = e;

                if (!shouldRetry(e, attempt)) {
                    throw e; // Doesn't retry for permanent failures
                }

                if (attempt < MAX_RETRIES) {
                    long backoffMs = calculateBackoffMs(attempt);
                    LOGGER.warn(
                            "Attempt {} failed for URL {}: {}. Retrying in {} ms...",
                            attempt,
                            sourceUrl,
                            e.getMessage(),
                            backoffMs);
                    Thread.sleep(backoffMs);
                } else {
                    LOGGER.error(
                            "All {} attempts failed for URL {}. Final error: {}",
                            MAX_RETRIES,
                            sourceUrl,
                            e.getMessage());
                }
            }
        }

        throw new IOException(
                "Failed to fetch content after " + MAX_RETRIES + " attempts: " + sourceUrl, lastException);
    }

    private String attemptFetch(String sourceUrl, int attempt) throws IOException, InterruptedException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sourceUrl))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                handleHttpError(response, sourceUrl);
            }

            String content = response.body();

            if (attempt > 1) {
                LOGGER.debug(
                        "Successfully fetched content from {} on attempt {}. Length: {} characters",
                        sourceUrl,
                        attempt,
                        content.length());
            } else {
                LOGGER.debug(
                        "Successfully fetched content from {}. Length: {} characters", sourceUrl, content.length());
            }

            return content;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid URL syntax: {}", sourceUrl, e);
            throw new IOException("The provided URL is malformed: " + sourceUrl, e);
        } catch (HttpTimeoutException e) {
            throw new IOException("Request timed out after " + REQUEST_TIMEOUT_SECONDS + "s: " + sourceUrl, e);
        }
    }

    /**
     * Determines if we should retry based on the exception type and attempt number.
     */
    private boolean shouldRetry(IOException e, int attempt) {
        if (attempt >= MAX_RETRIES) {
            return false;
        }

        // Don't retry client errors (permanent errors)
        if (e.getMessage().contains("401")
                || e.getMessage().contains("403")
                || e.getMessage().contains("404")
                || e.getMessage().contains("400")) {
            return false;
        }

        // Don't retry malformed URLs
        if (e.getMessage().contains("malformed")) {
            return false;
        }

        // Retry timeouts and server errors
        return e.getMessage().contains("timed out")
                || e.getMessage().contains("500")
                || e.getMessage().contains("502")
                || e.getMessage().contains("503")
                || e.getMessage().contains("504")
                || e instanceof HttpTimeoutException;
    }

    /**
     * Calculate exponential backoff with jitter: 500ms, 1000ms, 2000ms
     */
    private long calculateBackoffMs(int attempt) {
        long baseBackoff = 500L * (1L << (attempt - 1)); // 500ms * 2^(attempt-1)
        long jitter = (long) (Math.random() * 100); // NOSONAR
        return baseBackoff + jitter;
    }

    private void handleHttpError(HttpResponse<String> response, String url) throws IOException {
        String errorMessage =
                switch (response.statusCode()) {
                    case 401 -> "Access denied (401 Unauthorized).";
                    case 403 -> "Access forbidden (403 Forbidden). Please ensure the URL is accessible.";
                    case 404 -> "Not found (404). Please verify the URL is correct.";
                    case 500 -> "Internal server error (500). Server is having issues.";
                    case 502 -> "Bad gateway (502). Server received invalid response.";
                    case 503 -> "Service unavailable (503). Server is temporarily overloaded.";
                    case 504 -> "Gateway timeout (504). Server took too long to respond.";
                    default -> "Request failed with HTTP status code: " + response.statusCode();
                };
        throw new IOException(errorMessage + " URL: " + url);
    }
}
