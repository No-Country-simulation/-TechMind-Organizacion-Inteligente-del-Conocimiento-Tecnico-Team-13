package com.application.service;

import com.application.model.ChatMensaje;
import com.application.repository.ChatMensajeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Persiste el historial del Consultor IA (RAG chat) en public.chat_mensaje, para que sobreviva a
 * un refresh, un logout o un reinicio del servidor. Antes vivía solo en memoria en
 * AiConsultantView. "Nueva sesión" en la UI arranca un session_id nuevo; la conversación anterior
 * queda intacta en la tabla, solo se deja de mostrar.
 */
@Service
public class ChatHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ChatHistoryService.class);

    public record MensajeGuardado(String rol, String contenido, List<RagChatService.Citation> citas) {
    }

    public record SesionResumen(UUID sessionId, OffsetDateTime ultimoMensaje, long totalMensajes, String primeraPregunta) {
    }

    private final ChatMensajeRepository chatMensajeRepository;
    private final ObjectMapper objectMapper;

    public ChatHistoryService(ChatMensajeRepository chatMensajeRepository, ObjectMapper objectMapper) {
        this.chatMensajeRepository = chatMensajeRepository;
        this.objectMapper = objectMapper;
    }

    /** El session_id más reciente del usuario, o uno nuevo si nunca chateó antes. */
    public UUID cargarUltimaSesionOCrear(UUID userId) {
        return chatMensajeRepository.findFirstByUserIdOrderByFechaCreacionDesc(userId)
                .map(ChatMensaje::getSessionId)
                .orElseGet(UUID::randomUUID);
    }

    /** Conversaciones anteriores del usuario (una fila por session_id), para el diálogo de
     *  "Historial", más recientes primero. */
    public List<SesionResumen> listarSesiones(UUID userId) {
        return chatMensajeRepository.findSessionSummaryRowsByUserId(userId).stream()
                .map(row -> new SesionResumen(
                        (UUID) row[0],
                        toOffsetDateTime(row[1]),
                        ((Number) row[2]).longValue(),
                        (String) row[3]))
                .toList();
    }

    private OffsetDateTime toOffsetDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof OffsetDateTime odt) {
            return odt;
        }
        if (value instanceof Instant instant) {
            return instant.atOffset(ZoneOffset.UTC);
        }
        if (value instanceof Timestamp ts) {
            return ts.toInstant().atOffset(ZoneOffset.UTC);
        }
        throw new IllegalStateException("Tipo de fecha inesperado en fila de resumen de sesión: " + value.getClass());
    }

    public List<MensajeGuardado> cargarMensajes(UUID sessionId) {
        return chatMensajeRepository.findBySessionIdOrderByFechaCreacionAsc(sessionId).stream()
                .map(m -> new MensajeGuardado(m.getRol(), m.getContenido(), deserializarCitas(m.getCitas())))
                .toList();
    }

    public void guardarMensaje(UUID sessionId, UUID userId, String rol, String contenido, List<RagChatService.Citation> citas) {
        ChatMensaje mensaje = new ChatMensaje();
        mensaje.setSessionId(sessionId);
        mensaje.setUserId(userId);
        mensaje.setRol(rol);
        mensaje.setContenido(contenido);
        mensaje.setCitas(citas != null && !citas.isEmpty() ? serializarCitas(citas) : null);
        chatMensajeRepository.save(mensaje);
        log.info("Mensaje de chat guardado: sessionId={}, rol={}, {} cita(s)", sessionId, rol, citas != null ? citas.size() : 0);
    }

    private String serializarCitas(List<RagChatService.Citation> citas) {
        try {
            return objectMapper.writeValueAsString(citas);
        } catch (Exception e) {
            log.warn("No se pudieron serializar las citas del mensaje, se guarda sin ellas: {}", e.getMessage());
            return null;
        }
    }

    private List<RagChatService.Citation> deserializarCitas(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<RagChatService.Citation>>() {
            });
        } catch (Exception e) {
            log.warn("No se pudieron deserializar las citas de un mensaje guardado: {}", e.getMessage());
            return List.of();
        }
    }
}
