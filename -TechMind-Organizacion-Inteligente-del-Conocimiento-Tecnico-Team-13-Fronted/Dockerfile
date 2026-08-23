FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

# El build-frontend de Vaadin (activado por -Pproduction abajo) necesita más que src/: lee
# frontend/ (concept-graph.js, index.html), package.json/package-lock.json, tsconfig.json y
# vite.config.ts desde la raíz del proyecto. Copiar solo src/ (como antes) lo dejaba sin esos
# archivos y fallaba con "Failed to find the following files: - ./concept-graph.js".
COPY . .
# -Pproduction activa el goal build-frontend de Vaadin (genera flow-build-info.json y empaqueta
# el bundle del frontend). Sin este perfil el jar queda en modo desarrollo, que en runtime intenta
# levantar el dev server y falla porque el contenedor final solo tiene el .jar, no el proyecto
# Maven completo: "Failed to determine project directory for dev mode".
RUN ./mvnw -q -DskipTests -Pproduction clean package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar

# application.properties usa server.port=${PORT:8085}; sin esto, si el .env de despliegue
# olvida fijar PORT=8080, la app arranca bien pero en 8085 y el mapeo de puertos del contenedor
# (8080) queda apuntando a nada, sin ningún error visible. Fijarlo aquí lo hace correcto por
# defecto; el .env todavía puede sobreescribirlo si hace falta.
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
