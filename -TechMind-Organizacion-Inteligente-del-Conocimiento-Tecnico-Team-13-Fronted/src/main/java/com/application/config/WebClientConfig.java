package com.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${fastapi.base-url}")
    private String baseUrl;

    // Support both new names (fastapi.timeout.*) and existing ones (fastapi.connection-timeout / fastapi.read-timeout).
    // Fall back to the existing properties or to defaults (5000 ms) if none are set.
    @Value("${fastapi.timeout.connect:${fastapi.connection-timeout:5000}}")
    private int connectTimeoutMillis;

    @Value("${fastapi.timeout.response:${fastapi.read-timeout:5000}}")
    private int responseTimeoutMillis;

    @Bean
    public WebClient webClient() {
        // Configure connection provider with connection timeout
        ConnectionProvider provider = ConnectionProvider.builder("fastapi-connection-provider")
                .maxConnections(50) // Adjust as needed
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(30))
                .evictInBackground(Duration.ofSeconds(60))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .responseTimeout(Duration.ofMillis(responseTimeoutMillis)); // Response timeout for the whole exchange

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
