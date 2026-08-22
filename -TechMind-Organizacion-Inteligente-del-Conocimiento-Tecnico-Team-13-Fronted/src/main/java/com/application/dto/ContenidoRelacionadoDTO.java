package com.application.dto;

/** Un contenido relacionado por similitud de embeddings (pgvector), con el score que lo justifica. */
public record ContenidoRelacionadoDTO(String titulo, double similitud) {
}
