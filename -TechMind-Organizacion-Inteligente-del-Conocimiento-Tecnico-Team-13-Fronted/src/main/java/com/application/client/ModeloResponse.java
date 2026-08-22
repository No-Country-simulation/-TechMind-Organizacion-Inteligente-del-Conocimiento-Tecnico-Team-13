package com.application.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Espejo del contrato JSON del clasificador (Flask, ver ModeloClienteService/classifier.base-url):
 * {"categoria": ..., "probabilidad": ..., "palabras_clave": [...], "recomendaciones": [...]}.
 * El backend calcula sus propios contenidos relacionados vía embeddings de OpenAI + pgvector
 * (ver ContenidoService.buscarRelacionados) en vez de usar "recomendaciones" del clasificador,
 * pero se mapea igual por si algún consumidor futuro la necesita.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ModeloResponse(
        String categoria,
        Double probabilidad,
        @JsonProperty("palabras_clave") List<String> palabrasClave,
        List<String> recomendaciones
) {
}
