# Despliegue en OCI (backend)

Supabase ya resuelve Auth + Postgres/pgvector + Storage, y no hay ningún frontend separado en
Vercel: este repo (Spring Boot + Vaadin) **es** el frontend y el backend a la vez. Lo único que
necesita infraestructura propia en OCI son los **dos servicios** que corren código del equipo:

1. **`app` (este repo)** — Spring Boot + Vaadin, puerto 8080. Incluye ahora la capa de RAG
   (embeddings/chat de OpenAI, pgvector) además del login y el resto de vistas.
2. **`fastapi-classifier`** — el microservicio Python de Squad 2 (clasificador TF-IDF + LinearSVC,
   ver `Hackaton_LogiCore.ipynb`), puerto 8000. Vive en otro repo; aquí solo se documenta cómo
   desplegarlo *junto* al backend Java.

```
                         Internet
                            │
                    (80/443, TLS)
                            ▼
                    ┌───────────────┐
                    │     Nginx     │  reverse proxy + TLS (certbot)
                    └───────┬───────┘
                            │ :8080
                            ▼
                    ┌───────────────┐        :8000 (solo red interna,
                    │  app (Vaadin) │ ─────▶  nunca expuesto a Internet)
                    │  Spring Boot  │        ┌───────────────────┐
                    └───────┬───────┘        │ fastapi-classifier│
                            │                └───────────────────┘
                            │ HTTPS (salida)
              ┌─────────────┼─────────────────┐
              ▼                               ▼
        Supabase (Auth,                  OpenAI API
        Postgres+pgvector,               (embeddings +
        Storage)                         chat RAG)
```

`fastapi-classifier` solo lo llama `app` server-to-server (`ModeloClienteService`) — nunca el
navegador — así que no necesita puerto público ni entrada en Nginx. Eso simplifica bastante la
superficie de ataque.

## 1. Instancia de cómputo

Para un hackathon, una sola VM alcanza y cabe en el **Always Free tier** de OCI:

- Forma: `VM.Standard.A1.Flex` (ARM Ampere), 2 OCPU / 12 GB — o hasta 4 OCPU / 24 GB si necesitan
  más margen (el free tier da hasta 4 OCPU/24GB en total entre instancias A1).
- Imagen: Oracle Linux 9 o Ubuntu 22.04 (ambas soportadas por Ampere).
- Red: crear la VM en una VCN con un subnet público, IP pública asignada.
- **Nota real de OCI**: además del Security List de la consola, Oracle Linux trae `firewalld`/`iptables`
  activo por defecto y bloquea todo lo que no sea 22. Hay que abrir 80/443 también ahí
  (`firewall-cmd --add-port=80/tcp --permanent && firewall-cmd --add-port=443/tcp --permanent && firewall-cmd --reload`),
  si no la reverse proxy nunca es alcanzable aunque el Security List esté bien.

### Security List / NSG

| Puerto | Origen | Motivo |
|---|---|---|
| 22 | tu IP (no 0.0.0.0/0) | SSH |
| 80, 443 | 0.0.0.0/0 | Nginx (HTTP/TLS) |
| 8080, 8000 | — | **no abrir**, solo tráfico interno vía Docker network |

## 2. Docker en la VM

```bash
sudo dnf install -y dnf-utils   # Oracle Linux; en Ubuntu: apt-get install docker.io docker-compose-plugin
sudo dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker $USER   # relogueate para que aplique
```

## 3. Variables de entorno del backend

El repo ya trae `Dockerfile` y `docker-compose.yml` (arranca con `--spring.profiles.active=prod`,
pero no hay `application-prod.properties` — todo se resuelve por variables de entorno, que es lo
correcto en producción). Crear un `.env` **en la VM, nunca commiteado** con:

```bash
# Supabase (proyecto bnaqxitmvgmspufbkpvt)
SUPABASE_URL=https://bnaqxitmvgmspufbkpvt.supabase.co
SUPABASE_API_KEY=sb_publishable_...
SUPABASE_SERVICE_KEY=sb_secret_...
SUPABASE_JWKS_URL=https://bnaqxitmvgmspufbkpvt.supabase.co/auth/v1/.well-known/jwks.json
DB_URL=jdbc:postgresql://aws-0-us-east-2.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres.bnaqxitmvgmspufbkpvt
DB_PASSWORD=...

# FastAPI del clasificador (nombre del servicio en docker-compose, ver abajo)
FASTAPI_BASE_URL=http://fastapi-classifier:8000

# OpenAI (RAG)
OPENAI_API_KEY=sk-proj-...
OPENAI_EMBEDDING_MODEL=text-embedding-3-small
OPENAI_CHAT_MODEL=gpt-4o-mini

# App
APP_BASE_URL=https://tu-dominio.com
PORT=8080
```

## 4. docker-compose para los dos servicios + Nginx

Extender el `docker-compose.yml` existente (que hoy solo levanta `app`) así:

```yaml
services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    env_file: .env
    expose:
      - "8080"
    restart: unless-stopped
    depends_on:
      - fastapi-classifier

  fastapi-classifier:
    image: registro-de-squad2/fastapi-classifier:latest   # o build: context: ../ruta-al-repo-de-squad2
    expose:
      - "8000"
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
      - ./certbot/conf:/etc/letsencrypt:ro
      - ./certbot/www:/var/www/certbot:ro
    depends_on:
      - app
    restart: unless-stopped
```

`app` y `fastapi-classifier` usan `expose` (solo red interna de Docker), no `ports` — así
`FASTAPI_BASE_URL=http://fastapi-classifier:8000` resuelve por nombre de servicio y nunca queda
alcanzable desde fuera de la VM.

## 5. Nginx (reverse proxy + WebSocket para Vaadin)

Vaadin usa WebSocket/long-polling para el push de UI; sin las cabeceras `Upgrade`/`Connection` la
app carga pero se queda "colgada" en las actualizaciones. `nginx.conf`:

```nginx
server {
    listen 80;
    server_name tu-dominio.com;
    location /.well-known/acme-challenge/ { root /var/www/certbot; }
    location / { return 301 https://$host$request_uri; }
}

server {
    listen 443 ssl;
    server_name tu-dominio.com;
    ssl_certificate     /etc/letsencrypt/live/tu-dominio.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/tu-dominio.com/privkey.pem;

    location / {
        proxy_pass http://app:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 3600s;   # las respuestas RAG pueden tardar varios segundos
    }
}
```

TLS con certbot (modo webroot, sin parar Nginx):

```bash
sudo docker compose run --rm -v ./certbot/conf:/etc/letsencrypt -v ./certbot/www:/var/www/certbot \
  certbot/certbot certonly --webroot -w /var/www/certbot -d tu-dominio.com
```

## 6. Desplegar

```bash
git clone <este-repo> && cd -TechMind-...-Fronted
# copiar el .env con las variables de la sección 3
sudo docker compose up -d --build
sudo docker compose logs -f app   # confirmar que Flyway corrió la migración de "contenido" sin error
```

## 7. Checklist antes de dar por "funciona correctamente"

- [ ] `docker compose logs app` muestra la migración Flyway `V1__enable_pgvector_and_create_contenido.sql` aplicada sin error (si falla, correrla a mano desde Supabase SQL Editor — el archivo está pensado para copiar/pegar tal cual).
- [ ] `curl -I https://tu-dominio.com` responde 200 y la app carga en el navegador (prueba visual del login).
- [ ] Desde la VM, `docker compose exec app curl http://fastapi-classifier:8000/docs` (o el endpoint que exponga Squad 2) responde — confirma que Spring Boot sí llega al clasificador por red interna.
- [ ] Guardar un contenido de prueba desde "Añadir Contenido" y verificar que aparece con categoría y palabras clave (clasificador) y sin errores de embedding (OpenAI) en los logs.
- [ ] Preguntar algo en el Consultor IA y verificar que cita una fuente real de lo guardado.
- [ ] `OPENAI_API_KEY` y `SUPABASE_SERVICE_KEY` no aparecen en ningún archivo commiteado (solo en el `.env` de la VM).
