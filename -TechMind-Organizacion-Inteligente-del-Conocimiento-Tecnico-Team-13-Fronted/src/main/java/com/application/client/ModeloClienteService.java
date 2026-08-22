package com.application.client;

import com.application.exception.ModeloServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/** Cliente hacia el clasificador de contenido (Flask, endpoint POST /predict). */
@Service
public class ModeloClienteService {

    private static final Logger log = LoggerFactory.getLogger(ModeloClienteService.class);

    private final WebClient webClient;

    public ModeloClienteService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ModeloResponse> analizarContenido(String titulo, String texto) {
        Map<String, Object> requestBody = Map.of(
                "titulo", titulo,
                "texto", texto,
                "top_n", 5
        );

        return webClient.post()
                .uri("/predict")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(ModeloResponse.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(5))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                new ModeloServiceException("Clasificador no disponible tras reintentos", retrySignal.failure())))
                .doOnNext(respuesta -> log.info("Clasificador OK: titulo=\"{}\", categoria={}, probabilidad={}",
                        titulo, respuesta.categoria(), respuesta.probabilidad()))
                .onErrorResume(e -> {
                    log.warn("Clasificador no disponible para \"{}\": {}", titulo, e.getMessage());
                    return Mono.error(new ModeloServiceException("Error comunicándose con el clasificador: " + e.getMessage(), e));
                });
    }
}
