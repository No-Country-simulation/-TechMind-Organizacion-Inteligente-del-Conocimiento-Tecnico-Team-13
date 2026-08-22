package com.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ContenidoResponseDTO(
        Long id,
        String titulo,
        String texto,
        String categoria,
        Double probabilidad,
        List<String> palabrasClave,
        List<ContenidoRelacionadoDTO> contenidosRelacionados,
        LocalDateTime fechaCreacion
) {
}
