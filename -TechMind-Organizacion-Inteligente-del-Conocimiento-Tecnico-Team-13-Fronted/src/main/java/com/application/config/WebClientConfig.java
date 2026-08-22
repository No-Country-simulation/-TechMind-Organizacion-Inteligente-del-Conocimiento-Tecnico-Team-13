package com.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/** WebClient hacia el clasificador (Flask, ver ModeloClienteService). */
@Configuration
public class WebClientConfig {

    @Value("${classifier.base-url}")
    private String baseUrl;

    @Value("${classifier.timeout.connect:5000}")
    private int connectTimeoutMillis;

    @Value("${classifier.timeout.response:10000}")
    private int responseTimeoutMillis;

    @Bean
    public WebClient webClient() {
        ConnectionProvider provider = ConnectionProvider.builder("classifier-connection-provider")
                .maxConnections(50)
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(30))
                .evictInBackground(Duration.ofSeconds(60))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .responseTimeout(Duration.ofMillis(responseTimeoutMillis));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
