package com.redhat.sast.api.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class UrlValidationService {

    private final HttpClient httpClient;
    private final Duration requestTimeout;

    public UrlValidationService(
            @ConfigProperty(name = "url-validation.connection-timeout", defaultValue = "5s") Duration connectionTimeout,
            @ConfigProperty(name = "url-validation.request-timeout", defaultValue = "30s") Duration requestTimeout) {
        this.httpClient =
                HttpClient.newBuilder().connectTimeout(connectionTimeout).build();
        this.requestTimeout = requestTimeout;
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
                    .timeout(requestTimeout)
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            boolean isAccessible = response.statusCode() == 200;

            LOGGER.debug(
                    "URL accessibility check for '{}': status={}, accessible={}",
                    url,
                    response.statusCode(),
                    isAccessible);

            return isAccessible;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.debug("URL accessibility check interrupted for '{}': {}", url, e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.debug("Failed to check URL accessibility for '{}': {}", url, e.getMessage());
            return false;
        }
    }
}
