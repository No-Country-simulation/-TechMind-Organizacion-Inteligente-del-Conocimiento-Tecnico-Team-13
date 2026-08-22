package com.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.UUID;

/**
 * Acceso a Supabase Storage (bytes de archivos). El registro/lectura de contenido en si vive en
 * ContenidoService (JPA + pgvector, tabla public.contenido), no aqui: Storage y base de datos son
 * responsabilidades distintas y Supabase las expone por APIs distintas.
 */
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
        if (userId == null) {
            throw new IllegalArgumentException("El usuario autenticado no tiene un identificador válido");
        }

        String sanitizedFileName = sanitizeFileName(fileName);
        String storagePath = userId + "/" + sanitizedFileName;
        String endpoint = supabaseUrl + "/storage/v1/object/documentos_usuarios/" + storagePath;
        String resolvedMimeType = mimeType != null ? mimeType : "application/octet-stream";

        try {
            byte[] bytes = inputStream.readAllBytes();

            webClient.post()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("x-upsert", "true")
                    .contentType(MediaType.parseMediaType(resolvedMimeType))
                    .bodyValue(bytes)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return storagePath;
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al subir archivo a Supabase Storage: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo a Supabase Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Descarga el archivo del storage y devuelve su contenido en bytes.
     */
    public byte[] downloadFileFromStorage(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            throw new IllegalArgumentException("storagePath no puede ser nulo o vacío");
        }

        String endpoint = supabaseUrl + "/storage/v1/object/documentos_usuarios/" + storagePath;
        try {
            byte[] bytes = webClient.get()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return response.bodyToMono(byte[].class);
                        }
                        return response.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new RuntimeException("Error al descargar archivo de Supabase: status=" + response.statusCode() + ", body=" + body)));
                    })
                    .block();

            if (bytes == null || bytes.length == 0) {
                throw new RuntimeException("El archivo descargado está vacío o no legible: " + storagePath);
            }
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException("Error al descargar archivo de Supabase Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un archivo de Storage. No lanza si falla (best-effort, se llama tras borrar el
     * registro de contenido en Postgres y no queremos que un archivo huérfano rompa el flujo).
     */
    public void deleteFileFromStorage(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return;
        }
        String endpoint = supabaseUrl + "/storage/v1/object/documentos_usuarios/" + storagePath;
        try {
            webClient.delete()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            System.err.println("No se pudo eliminar el archivo de storage: " + storagePath + " -> " + e.getMessage());
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "documento.pdf";
        }
        String cleaned = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return cleaned.isBlank() ? "documento.pdf" : cleaned;
    }
}
