-- Stub minimo del esquema auth de Supabase para los tests de integracion.
-- Un Postgres "pelado" (el contenedor de Testcontainers) no trae el esquema auth que Supabase
-- provisiona automaticamente; la migracion real (V1__enable_pgvector_and_create_contenido.sql)
-- referencia auth.users(id) con una FK, asi que aqui creamos justo lo necesario para que esa FK
-- se pueda crear antes de que Flyway corra.
CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE IF NOT EXISTS auth.users (
    id UUID PRIMARY KEY,
    email VARCHAR(255)
);
