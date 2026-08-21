package com.application.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ModeloResponse(
    String categoria,
    double probabilidad,
    @JsonProperty("palabras_clave") List<String> palabrasClave,
    List<String> recomendaciones
) {}
