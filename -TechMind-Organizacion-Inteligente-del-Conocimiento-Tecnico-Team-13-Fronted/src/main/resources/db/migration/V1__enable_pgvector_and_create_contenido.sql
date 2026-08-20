-- V1: extension pgvector + tabla unificada de contenido tecnico (RAG)
--
-- Reemplaza la tabla "contenidos" (plural) a la que antes se escribia por REST/PostgREST
-- desde SupabaseService. A partir de esta version, el guardado y la lectura de contenido
-- pasan por JPA (ContenidoRepository) contra esta tabla "contenido" (singular).
--
-- Puedes correr este archivo tal cual en Supabase Dashboard -> SQL Editor si prefieres
-- aplicarlo a mano en vez de dejar que Flyway lo aplique al levantar la app.

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS public.contenido (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id               UUID NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
    titulo                VARCHAR(200) NOT NULL,
    texto                 TEXT NOT NULL,
    tipo_contenido        VARCHAR(50) NOT NULL DEFAULT 'texto_plano',
    storage_path          TEXT,
    estado_procesamiento  VARCHAR(30) NOT NULL DEFAULT 'pendiente',
    categoria             VARCHAR(100),
    palabras_clave        TEXT[] NOT NULL DEFAULT '{}',
    -- text-embedding-3-small de OpenAI produce vectores de 1536 dimensiones.
    embedding             vector(1536),
    fecha_creacion        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_contenido_user_id ON public.contenido (user_id);

-- HNSW: no necesita "entrenarse" con datos existentes (a diferencia de ivfflat), lo cual
-- conviene porque la tabla arranca vacia y crece contenido a contenido durante el hackathon.
CREATE INDEX IF NOT EXISTS idx_contenido_embedding_cosine ON public.contenido
    USING hnsw (embedding vector_cosine_ops);

COMMENT ON TABLE public.contenido IS
    'Contenido tecnico enriquecido: clasificacion (FastAPI) + embedding OpenAI para busqueda semantica / RAG.';
COMMENT ON COLUMN public.contenido.embedding IS
    'Embedding OpenAI text-embedding-3-small (1536 dim) del texto (o titulo+texto) para similitud coseno.';
