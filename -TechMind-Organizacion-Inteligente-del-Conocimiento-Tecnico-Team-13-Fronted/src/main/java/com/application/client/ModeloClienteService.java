package com.application.client;

import com.application.exception.ModeloServiceException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Service
public class ModeloClienteService {

    private final WebClient webClient;

    public ModeloClienteService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ModeloResponse> analizarContenido(String titulo, String texto) {
        Map<String, String> requestBody = Map.of(
                "titulo", titulo,
                "texto", texto
        );

        return webClient.post()
                .uri("/analizar")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(ModeloResponse.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)) // Max 2 retries, 1-second backoff
                        .maxBackoff(Duration.ofSeconds(5))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                new ModeloServiceException("FastAPI service is unavailable after retries", retrySignal.failure())))
                .onErrorResume(e -> {
                    // Fallback: Catch any errors not handled by retry, including initial connection errors
                    return Mono.error(new ModeloServiceException("Error communicating with FastAPI service", e));
                });
    }
}
