package com.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ContenidoResponseDTO(
        Long id,
        String titulo,
        String texto,
        String categoria,
        List<String> palabrasClave,
        List<String> contenidosRelacionados,
        LocalDateTime fechaCreacion
) {
}
