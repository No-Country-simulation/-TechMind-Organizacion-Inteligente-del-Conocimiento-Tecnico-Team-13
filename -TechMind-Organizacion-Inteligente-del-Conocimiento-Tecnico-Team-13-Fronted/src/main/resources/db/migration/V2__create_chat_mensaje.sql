-- V2: historial persistente del Consultor IA (RAG chat).
--
-- Antes el historial vivia solo en memoria (List<ChatTurn> en AiConsultantView, atado a la
-- sesion de Vaadin), se perdia al recargar la pagina, cerrar sesion o reiniciar el servidor.
-- Esta tabla guarda cada turno (pregunta del usuario / respuesta del asistente) para poder
-- recargar la ultima conversacion de cada usuario al volver a entrar. "Nueva sesion" en la UI
-- arranca un session_id nuevo; la conversacion anterior queda intacta en la tabla, no se borra.

CREATE TABLE IF NOT EXISTS public.chat_mensaje (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id      UUID NOT NULL,
    user_id         UUID NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
    rol             VARCHAR(20) NOT NULL,
    contenido       TEXT NOT NULL,
    -- Citas (RagChatService.Citation: id, titulo, similitud) serializadas como JSON; null si el
    -- mensaje es del usuario o si la respuesta no citó ninguna fuente.
    citas           TEXT,
    fecha_creacion  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Localizar rapido la sesion mas reciente de un usuario (recarga de historial al entrar).
CREATE INDEX IF NOT EXISTS idx_chat_mensaje_user_fecha ON public.chat_mensaje (user_id, fecha_creacion);

-- Cargar todos los mensajes de una sesion en orden cronologico.
CREATE INDEX IF NOT EXISTS idx_chat_mensaje_session_fecha ON public.chat_mensaje (session_id, fecha_creacion);

COMMENT ON TABLE public.chat_mensaje IS
    'Historial persistente del Consultor IA: un registro por turno (user/assistant), agrupado por session_id.';
