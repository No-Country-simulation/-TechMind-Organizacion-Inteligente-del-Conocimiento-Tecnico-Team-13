package com.application.controller;

import com.application.dto.ContenidoRequestDTO;
import com.application.service.ContenidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sprint 3, tarea 4: orquesta recepción + validación + FastAPI + persistencia + respuesta.
 *
 * NOTA: userId se recibe como parámetro de la petición porque hoy no hay verificación de JWT en
 * este endpoint (la autenticación de la app corre contra la API de Supabase Auth vía sesión de
 * Vaadin, no contra este controller). Antes de exponer este endpoint fuera de la propia app
 * (Postman/Squad 2 en producción) hay que validar el JWT de Supabase contra supabase.jwks-url.
 */
@RestController
@RequestMapping("/contenido")
public class ContenidoController {

    private final ContenidoService contenidoService;

    public ContenidoController(ContenidoService contenidoService) {
        this.contenidoService = contenidoService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestParam UUID userId,
                                                       @Valid @RequestBody ContenidoRequestDTO request) {
        ContenidoService.GuardadoResult resultado = contenidoService.procesarYGuardar(userId, request, "texto_plano", null);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contenido", resultado.contenido());
        body.put("posiblesDuplicados", resultado.posiblesDuplicados());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
