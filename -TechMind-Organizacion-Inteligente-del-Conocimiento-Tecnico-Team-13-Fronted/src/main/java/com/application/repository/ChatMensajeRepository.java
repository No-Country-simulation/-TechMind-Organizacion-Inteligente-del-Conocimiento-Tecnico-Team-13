package com.application.repository;

import com.application.model.ChatMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatMensajeRepository extends JpaRepository<ChatMensaje, Long> {

    List<ChatMensaje> findBySessionIdOrderByFechaCreacionAsc(UUID sessionId);

    Optional<ChatMensaje> findFirstByUserIdOrderByFechaCreacionDesc(UUID userId);

    /** Las 10 conversaciones más recientes del usuario (una fila por session_id), para el diálogo
     *  de "Historial": fecha del último mensaje, cuántos mensajes tiene, y la primera pregunta
     *  como vista previa. Ordenado por actividad más reciente primero.
     *
     *  Devuelve Object[] (no una interfaz de proyección) a propósito: con GROUP BY + COUNT(*) +
     *  una subquery correlacionada, la proyección por interfaz de Spring Data para queries
     *  nativas mapeaba total_mensajes como null (NullPointerException al hacer .longValue() en
     *  ChatHistoryService) — un problema conocido de esa combinación con Hibernate 6.2.x. Con
     *  Object[] se decodifica el valor a mano en el service, sin depender de ese binding. */
    @Query(value = """
            SELECT session_id,
                   MAX(fecha_creacion) AS ultimo_mensaje,
                   COUNT(*) AS total_mensajes,
                   (SELECT contenido FROM public.chat_mensaje m2
                    WHERE m2.session_id = m1.session_id AND m2.rol = 'user'
                    ORDER BY m2.fecha_creacion ASC LIMIT 1) AS primera_pregunta
            FROM public.chat_mensaje m1
            WHERE user_id = :userId
            GROUP BY session_id
            ORDER BY MAX(fecha_creacion) DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findSessionSummaryRowsByUserId(@Param("userId") UUID userId);
}
