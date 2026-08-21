package com.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MlServiceClient {

    private static final String ML_API_URL = "http://localhost:5000/predict"; // change if needed
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MlServiceClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<MlPredictionResult> getPredictionAsync(String titulo, String texto) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("titulo", titulo != null ? titulo : "");
                requestBody.put("texto", texto != null ? texto : "");
                requestBody.put("top_n", 5);

                String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                        .uri(URI.create(ML_API_URL))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(20))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                // Log request/response for debugging
                System.out.println("[MlServiceClient] Request to " + ML_API_URL + ": " + jsonRequestBody);
                System.out.println("[MlServiceClient] Response code: " + response.statusCode());
                System.out.println("[MlServiceClient] Response body: " + response.body());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), MlPredictionResult.class);
                } else {
                    throw new RuntimeException("ML API error: " + response.statusCode() + " -> " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error calling ML service: " + e.getMessage(), e);
            }
        });
    }

    // New: return raw parsed map so the UI can render rich structured responses
    public CompletableFuture<Map<String, Object>> getPredictionMapAsync(String titulo, String texto) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("titulo", titulo != null ? titulo : "");
                requestBody.put("texto", texto != null ? texto : "");
                requestBody.put("top_n", 5);

                String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                        .uri(URI.create(ML_API_URL))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(20))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                // Log request/response for debugging
                System.out.println("[MlServiceClient] Request to " + ML_API_URL + ": " + jsonRequestBody);
                System.out.println("[MlServiceClient] Response code: " + response.statusCode());
                System.out.println("[MlServiceClient] Response body: " + response.body());

                if (response.statusCode() == 200) {
                    Map<String, Object> map = objectMapper.readValue(response.body(), Map.class);
                    return map;
                } else {
                    throw new RuntimeException("ML API error: " + response.statusCode() + " -> " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error calling ML service: " + e.getMessage(), e);
            }
        });
    }

    public static class MlPredictionResult {
        public String categoria;
        public double probabilidad;
        public String[] palabras_clave;
        public String[] recomendaciones;

        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        public double getProbabilidad() { return probabilidad; }
        public void setProbabilidad(double probabilidad) { this.probabilidad = probabilidad; }
        public String[] getPalabras_clave() { return palabras_clave; }
        public void setPalabras_clave(String[] palabras_clave) { this.palabras_clave = palabras_clave; }
        public String[] getRecomendaciones() { return recomendaciones; }
        public void setRecomendaciones(String[] recomendaciones) { this.recomendaciones = recomendaciones; }

        @Override
        public String toString() {
            String kws = palabras_clave == null ? "" : String.join(", ", palabras_clave);
            return "{Categoria: " + categoria + ", Probabilidad: " + probabilidad + ", Palabras Clave: " + kws + "}";
        }
    }
}
