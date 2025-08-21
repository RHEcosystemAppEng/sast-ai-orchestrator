package com.redhat.sast.api.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class UrlValidationService {

    private final HttpClient httpClient;

    public UrlValidationService() {
        this.httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public boolean isUrlAccessible(String url) {
        if (url == null || url.trim().isEmpty()) {
            LOGGER.debug("URL is null or empty, returning false");
            return false;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            boolean isAccessible = response.statusCode() == 200;

            LOGGER.debug(
                    "URL accessibility check for '{}': status={}, accessible={}",
                    url,
                    response.statusCode(),
                    isAccessible);

            return isAccessible;
        } catch (Exception e) {
            LOGGER.debug("Failed to check URL accessibility for '{}': {}", url, e.getMessage());
            return false;
        }
    }
}
