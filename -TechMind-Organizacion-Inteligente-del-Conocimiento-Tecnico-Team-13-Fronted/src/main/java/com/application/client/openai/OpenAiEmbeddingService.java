package com.application.client.openai;

import com.application.exception.ModeloServiceException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Genera embeddings con la API de OpenAI (text-embedding-3-small, 1536 dim) para busqueda
 * semantica / RAG sobre la tabla contenido (ver ContenidoRepository.findTopSimilar).
 */
@Service
public class OpenAiEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiEmbeddingService.class);

    private final WebClient openAiWebClient;
    private final String model;

    public OpenAiEmbeddingService(@Qualifier("openAiWebClient") WebClient openAiWebClient,
                                   @Value("${openai.embedding-model}") String model) {
        this.openAiWebClient = openAiWebClient;
        this.model = model;
    }

    public float[] embed(String text) {
        long inicio = System.currentTimeMillis();
        String preview = text.length() > 60 ? text.substring(0, 60) + "…" : text;
        try {
            EmbeddingResponse response = openAiWebClient.post()
                    .uri("/v1/embeddings")
                    .bodyValue(Map.of("model", model, "input", text))
                    .retrieve()
                    .bodyToMono(EmbeddingResponse.class)
                    .block(Duration.ofSeconds(20));

            if (response == null || response.data() == null || response.data().isEmpty()) {
                throw new ModeloServiceException("OpenAI no devolvió ningún embedding");
            }

            List<Double> vector = response.data().get(0).embedding();
            float[] result = new float[vector.size()];
            for (int i = 0; i < vector.size(); i++) {
                result[i] = vector.get(i).floatValue();
            }
            log.info("Embedding generado: modelo={}, dim={}, {} ms, texto=\"{}\" ({} caracteres totales)",
                    model, result.length, System.currentTimeMillis() - inicio, preview, text.length());
            return result;
        } catch (ModeloServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Fallo generando embedding tras {} ms para texto=\"{}\": {}",
                    System.currentTimeMillis() - inicio, preview, e.getMessage());
            throw new ModeloServiceException("Error generando embedding con OpenAI: " + e.getMessage(), e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EmbeddingResponse(List<EmbeddingData> data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EmbeddingData(List<Double> embedding) {
    }
}
