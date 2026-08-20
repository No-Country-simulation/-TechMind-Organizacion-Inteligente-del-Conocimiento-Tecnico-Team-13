package com.application.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Espejo del contrato JSON fijado con Data Science (ver notebook Hackaton_LogiCore.ipynb,
 * sección 0.6): {"categoria": ..., "probabilidad": ..., "palabras_clave": [...]}.
 * No mapeamos "recomendaciones": el backend calcula sus propios contenidos relacionados vía
 * embeddings de OpenAI + pgvector (ver ContenidoService), independientes del clasificador.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ModeloResponse(
        String categoria,
        Double probabilidad,
        @JsonProperty("palabras_clave") List<String> palabrasClave
) {
}
