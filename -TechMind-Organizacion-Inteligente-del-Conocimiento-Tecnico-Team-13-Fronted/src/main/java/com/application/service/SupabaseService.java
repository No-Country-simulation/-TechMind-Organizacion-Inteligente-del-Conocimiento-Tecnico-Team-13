package com.application.service;

import com.application.model.Content;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

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

        System.out.println("[SupabaseStorage] Iniciando upload: userId=" + userId + ", bucket=documentos_usuarios, path=" + storagePath + ", mimeType=" + resolvedMimeType);

        try {
            byte[] bytes = inputStream.readAllBytes();
            System.out.println("[SupabaseStorage] bytes=" + bytes.length + ", endpoint=" + endpoint);

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

            System.out.println("[SupabaseStorage] Upload OK para path=" + storagePath);
            return storagePath;
        } catch (WebClientResponseException e) {
            System.err.println("[SupabaseStorage] ERROR 400/response: status=" + e.getStatusCode() + ", endpoint=" + endpoint);
            System.err.println("[SupabaseStorage] body=" + e.getResponseBodyAsString());
            System.err.println("[SupabaseStorage] headers=" + e.getHeaders());
            throw new RuntimeException("Error al subir archivo a Supabase Storage: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            System.err.println("[SupabaseStorage] ERROR general: endpoint=" + endpoint + ", message=" + e.getMessage());
            throw new RuntimeException("Error al subir archivo a Supabase Storage: " + e.getMessage(), e);
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "documento.pdf";
        }

        String cleaned = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleaned.isBlank()) {
            return "documento.pdf";
        }
        return cleaned;
    }

    /**
     * Registra la entrada en la tabla 'contenidos' en PostgreSQL Supabase
     */
    public UUID registerContentRecord(UUID userId, String titulo, String tipoContenido, String textoPlano, String storagePath) {
        String endpoint = supabaseUrl + "/rest/v1/contenidos";

        if (userId == null) {
            throw new IllegalArgumentException("El usuario autenticado no tiene un identificador válido");
        }

        String safeTitle = titulo != null ? titulo.trim() : "";
        if (safeTitle.isBlank()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }

        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("user_id", userId);
        payload.put("titulo", safeTitle);
        payload.put("tipo_contenido", normalizeCategory(tipoContenido));
        if (textoPlano != null && !textoPlano.isBlank()) {
            payload.put("texto_plano", textoPlano.trim());
        }
        if (storagePath != null && !storagePath.isBlank()) {
            payload.put("storage_path", storagePath.trim());
        }
        payload.put("estado_procesamiento", "pendiente");

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("No se pudo serializar el payload JSON para Supabase: " + e.getMessage(), e);
        }

        System.out.println("[SupabaseDB] JSON Real enviado: " + jsonPayload);

        try {
            Map<String, Object>[] response = webClient.post()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("Prefer", "return=representation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonPayload)
                    .retrieve()
                    .bodyToMono(Map[].class)
                    .block();

            System.out.println("[SupabaseDB] Response code: 201/OK handled by WebClient");

            if (response != null && response.length > 0) {
                Object id = response[0].get("id");
                if (id != null) {
                    System.out.println("[SupabaseDB] Inserción exitosa en contenidos, id=" + id);
                    return UUID.fromString(id.toString());
                }
            }

            throw new RuntimeException("Supabase respondió sin un ID válido para el contenido insertado.");
        } catch (WebClientResponseException e) {
            System.err.println("[SupabaseDB] Response Code: " + e.getStatusCode());
            System.err.println("[SupabaseDB] Response Body: " + e.getResponseBodyAsString());
            throw new RuntimeException("Error al registrar contenido en Supabase: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error al registrar contenido en Supabase: " + e.getMessage(), e);
        }
    }

    private String normalizeCategory(String tipoContenido) {
        if (tipoContenido == null || tipoContenido.isBlank()) {
            System.err.println("[SupabaseDB] tipo_contenido nulo/vacío, se usa valor por defecto: Q/A");
            return "Q/A";
        }

        return tipoContenido.trim();
    }

    private String normalizeNullableText(String textoPlano) {
        if (textoPlano == null || textoPlano.isBlank()) {
            return null;
        }
        return textoPlano.trim();
    }

    private String normalizeNullableStoragePath(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            System.err.println("[SupabaseDB] storage_path nulo/vacío para el registro");
            return null;
        }
        return storagePath.trim();
    }

    /**
     * Obtiene todos los contenidos para un usuario específico
     */
    public List<Content> getContentsForUser(UUID userId) {
        String endpoint = supabaseUrl + "/rest/v1/contenidos?user_id=eq." + userId;

        try {
            return webClient.get()
                    .uri(endpoint + "&order=created_at.desc")
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

    /**
     * Actualiza la categoría (tipo_contenido) de un contenido específico
     */
    public void updateContentCategory(UUID contentId, String newCategory) {
        if (contentId == null) {
            throw new IllegalArgumentException("contentId no puede ser nulo");
        }

        String endpoint = supabaseUrl + "/rest/v1/contenidos?id=eq." + contentId;
        Map<String, Object> payload = Map.of("tipo_contenido", normalizeCategory(newCategory));

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            webClient.patch()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("Prefer", "return=representation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonPayload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar la categoría en Supabase: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza el título y categoría de un contenido específico
     */
    public void updateContent(UUID contentId, String newTitle, String newCategory) {
        if (contentId == null) {
            throw new IllegalArgumentException("contentId no puede ser nulo");
        }

        String endpoint = supabaseUrl + "/rest/v1/contenidos?id=eq." + contentId;
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        if (newTitle != null && !newTitle.isBlank()) {
            payload.put("titulo", newTitle.trim());
        }
        if (newCategory != null && !newCategory.isBlank()) {
            payload.put("tipo_contenido", normalizeCategory(newCategory));
        }

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            webClient.patch()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("Prefer", "return=representation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonPayload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el contenido en Supabase: " + e.getMessage(), e);
        }
    }

    /**
     * Mueve una lista de contenidos a una nueva categoría
     */
    public void moveContentsToCategory(List<UUID> contentIds, String targetCategory) {
        if (contentIds == null || contentIds.isEmpty() || targetCategory == null || targetCategory.isBlank()) {
            return;
        }

        for (UUID id : contentIds) {
            updateContentCategory(id, targetCategory);
        }
    }

    /**
     * Renombra una categoría existente para todos los contenidos de un usuario
     */
    public void renameCategoryForUser(UUID userId, String oldCategory, String newCategory) {
        if (userId == null || oldCategory == null || newCategory == null) {
            return;
        }

        String endpoint = supabaseUrl + "/rest/v1/contenidos?user_id=eq." + userId + "&tipo_contenido=eq." + oldCategory;
        Map<String, Object> payload = Map.of("tipo_contenido", normalizeCategory(newCategory));

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            webClient.patch()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonPayload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Error al renombrar la categoría en Supabase: " + e.getMessage(), e);
        }
    }

    public void deleteContentForUser(UUID contentId, String storagePath) {
        if (contentId != null) {
            deleteContentRecord(contentId);
        }

        if (storagePath != null && !storagePath.isBlank()) {
            deleteFileFromStorage(storagePath);
        }
    }

    private void deleteContentRecord(UUID contentId) {
        String endpoint = supabaseUrl + "/rest/v1/contenidos?id=eq." + contentId;

        try {
            webClient.delete()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el registro del contenido en Supabase: " + e.getMessage(), e);
        }
    }

    private void deleteFileFromStorage(String storagePath) {
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
                        System.out.println("[SupabaseStorage] download status=" + response.statusCode() + ", path=" + storagePath);
                        response.headers().asHttpHeaders().forEach((k, v) -> System.out.println("[SupabaseStorage] header: " + k + "=" + v));
                        if (response.statusCode().is2xxSuccessful()) {
                            return response.bodyToMono(byte[].class);
                        }
                        return response.bodyToMono(String.class).flatMap(body -> {
                            return Mono.error(new RuntimeException("Error al descargar archivo de Supabase: status=" + response.statusCode() + ", body=" + body));
                        });
                    })
                    .block();

            if (bytes == null || bytes.length == 0) {
                throw new RuntimeException("El archivo descargado está vacío o no legible: " + storagePath);
            }

            System.out.println("[SupabaseStorage] downloaded bytes=" + bytes.length + ", path=" + storagePath);
            return bytes;
        } catch (Exception e) {
            System.err.println("[SupabaseStorage] Error al descargar archivo: " + e.getMessage());
            throw new RuntimeException("Error al descargar archivo de Supabase Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Descarga un archivo, extrae título y texto plano, y registra el contenido en la tabla 'contenidos'.
     * Devuelve el UUID del registro creado.
     */
    public UUID extractAndRegisterContent(UUID userId, String storagePath, String originalFilename, String tipoContenido, String overrideTitle) {
        try {
            byte[] bytes = downloadFileFromStorage(storagePath);
            DocumentExtractor.DocumentData data = DocumentExtractor.extractFromBytes(bytes, originalFilename != null ? originalFilename : storagePath);

            String extractedTitle = data.title();
            String title = (overrideTitle != null && !overrideTitle.isBlank()) ? overrideTitle.trim() : extractedTitle;
            String content = data.content();

            return registerContentRecord(userId, title, tipoContenido, content, storagePath);
        } catch (Exception e) {
            throw new RuntimeException("Error al extraer y registrar contenido: " + e.getMessage(), e);
        }
    }
}
