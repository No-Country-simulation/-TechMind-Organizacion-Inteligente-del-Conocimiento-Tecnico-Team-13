package com.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
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

}
