package com.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContenidoRequestDTO(
        @NotBlank(message = "El título no puede estar vacío")
        @Size(max = 200, message = "El título no puede tener más de 200 caracteres")
        String titulo,

        @NotBlank(message = "El texto no puede estar vacío")
        @Size(max = 20000, message = "El texto no puede tener más de 20000 caracteres")
        String texto
) {
}
