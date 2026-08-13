package com.application.service;

import com.application.model.Content;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseService {

    private final WebClient webClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service.role.key}")
    private String serviceRoleKey;

    public SupabaseService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Sube un archivo directamente a Supabase Storage
     */
    public String uploadFileToStorage(UUID userId, String fileName, InputStream inputStream, String mimeType) {
        String storagePath = userId.toString() + "/" + System.currentTimeMillis() + "_" + fileName;
        String endpoint = supabaseUrl + "/storage/v1/object/documentos_usuarios/" + storagePath;

        try {
            byte[] bytes = inputStream.readAllBytes();

            webClient.post()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .contentType(MediaType.parseMediaType(mimeType != null ? mimeType : "application/octet-stream"))
                    .bodyValue(bytes)
                    .retrieve()
                    .toBodilessEntity()
                    .block(); // Make the call synchronous

            return storagePath;
        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo a Supabase Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Registra la entrada en la tabla 'contenidos' en PostgreSQL Supabase
     */
    public UUID registerContentRecord(UUID userId, String titulo, String tipoContenido, String textoPlano, String storagePath) {
        String endpoint = supabaseUrl + "/rest/v1/contenidos";

        Map<String, Object> payload = Map.of(
                "user_id", userId,
                "titulo", titulo,
                "tipo_contenido", tipoContenido,
                "texto_plano", textoPlano != null ? textoPlano : "",
                "storage_path", storagePath != null ? storagePath : "",
                "estado_procesamiento", "pendiente"
        );

        try {
            Map<String, Object>[] response = webClient.post()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("Prefer", "return=representation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map[].class)
                    .block(); // Make the call synchronous

            if (response != null && response.length > 0) {
                return UUID.fromString((String) response[0].get("id"));
            }
            throw new RuntimeException("Respuesta vacía de Supabase");
        } catch (Exception e) {
            throw new RuntimeException("Error al registrar en la base de datos Supabase: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los contenidos para un usuario específico
     */
    public List<Content> getContentsForUser(UUID userId) {
        String endpoint = supabaseUrl + "/rest/v1/contenidos?user_id=eq." + userId;

        try {
            return webClient.get()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(Content.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener contenidos de Supabase: " + e.getMessage(), e);
        }
    }
}
