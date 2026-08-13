package com.application.client;

import java.util.List;

public record ModeloResponse(
    String categoria,
    List<String> palabrasClave,
    List<String> contenidosRelacionados
) {}
