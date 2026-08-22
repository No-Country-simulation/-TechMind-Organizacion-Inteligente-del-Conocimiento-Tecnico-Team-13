package com.application.data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StaticConceptData {

    public record NodeDto(String id, String label, String group, String color) {}
    public record EdgeDto(String from, String to) {}

    public record ConceptDetail(
            String id,
            String name,
            String category,
            String description,
            int relationsCount,
            List<String> relatedConcepts,
            List<String> contents) {
    }

    public static final Map<String, String> CATEGORY_COLORS = Map.ofEntries(
            Map.entry("Backend", "#FF6B6B"),
            Map.entry("Security", "#00B894"),
            Map.entry("Arquitectura", "#F97316"),
            Map.entry("Frontend", "#FFD166"),
            Map.entry("DevOps", "#4D96FF"),
            Map.entry("Cloud", "#6BFFB8"),
            Map.entry("Data Science", "#A96BFF")
    );

    public static final Map<String, String> CATEGORY_BACKGROUND_COLORS = Map.ofEntries(
            Map.entry("Backend", "hsla(0, 100%, 71%, 0.1)"),
            Map.entry("Security", "hsla(162, 100%, 36%, 0.1)"),
            Map.entry("Arquitectura", "hsla(24, 95%, 53%, 0.1)"),
            Map.entry("Frontend", "hsla(45, 100%, 70%, 0.1)"),
            Map.entry("DevOps", "hsla(217, 100%, 65%, 0.1)"),
            Map.entry("Cloud", "hsla(150, 100%, 71%, 0.1)"),
            Map.entry("Data Science", "hsla(271, 100%, 71%, 0.1)")
    );

    public static final List<NodeDto> NODES = List.of(
            node("spring-boot", "Spring Boot", "Backend"),
            node("kafka", "Kafka", "Backend"),
            node("jwt", "JWT", "Security"),
            node("oauth2", "OAuth2", "Security"),
            node("microservices", "Microservices", "Arquitectura"),
            node("react", "React", "Frontend"),
            node("typescript", "TypeScript", "Frontend"),
            node("kubernetes", "Kubernetes", "DevOps"),
            node("terraform", "Terraform", "DevOps"),
            node("docker", "Docker", "DevOps"),
            node("oci", "OCI", "Cloud"),
            node("python", "Python", "Data Science"),
            node("pandas", "Pandas", "Data Science")
    );

    public static final List<EdgeDto> EDGES = List.of(
            new EdgeDto("spring-boot", "kafka"),
            new EdgeDto("spring-boot", "jwt"),
            new EdgeDto("spring-boot", "oauth2"),
            new EdgeDto("spring-boot", "microservices"),
            new EdgeDto("jwt", "oauth2"),
            new EdgeDto("oauth2", "microservices"),
            new EdgeDto("kafka", "microservices"),
            new EdgeDto("microservices", "kubernetes"),
            new EdgeDto("microservices", "react"),
            new EdgeDto("react", "typescript"),
            new EdgeDto("kubernetes", "terraform"),
            new EdgeDto("kubernetes", "docker"),
            new EdgeDto("terraform", "oci"),
            new EdgeDto("docker", "oci"),
            new EdgeDto("python", "pandas")
    );

    private static final Map<String, String> CONCEPT_DESCRIPTIONS = Map.ofEntries(
            Map.entry("kafka", "Plataforma distribuida de transmisión de eventos de alto rendimiento (event streaming), diseñada para publicar, suscribirse, almacenar y procesar flujos de datos en tiempo real de manera masiva y tolerante a fallos."),
            Map.entry("spring-boot", "Framework de desarrollo en Java de código abierto que simplifica la creación de aplicaciones e microservicios autónomos y listos para producción, minimizando la configuración mediante convenciones predefinidas."),
            Map.entry("terraform", "Herramienta de Infraestructura como Código (IaC) que permite definir, aprovisionar y gestionar recursos de infraestructura en múltiples proveedores de la nube mediante archivos de configuración declarativos."),
            Map.entry("kubernetes", "Plataforma de orquestación de contenedores de código abierto que automatiza el despliegue, el escalado, la gestión y la disponibilidad de aplicaciones en contenedores en entornos distribuidos."),
            Map.entry("microservices", "Arquitectura de software que estructura una aplicación como un conjunto de servicios pequeños, independientes y acoplados de forma débil, donde cada uno ejecuta un proceso único y se comunica mediante APIs ligeras."),
            Map.entry("typescript", "Superset de JavaScript desarrollado por Microsoft que añade tipado estático opcional y características avanzadas de POO, mejorando la detección de errores en tiempo de compilación y el mantenimiento del código."),
            Map.entry("oci", "Plataforma de servicios en la nube de Oracle que ofrece cómputo, almacenamiento, redes y bases de datos optimizadas para cargas de trabajo empresariales de alto rendimiento."),
            Map.entry("python", "Lenguaje de programación interpretado de alto nivel y propósitos múltiples, reconocido por su sintaxis limpia y su amplio ecosistema para ciencia de datos, backend, automatización e IA."),
            Map.entry("docker", "Plataforma de virtualización a nivel de sistema operativo que permite empaquetar aplicaciones y todas sus dependencias dentro de contenedores aislados, garantizando un comportamiento idéntico en cualquier entorno."),
            Map.entry("jwt", "Estándar abierto (RFC 7519) basado en JSON para transmitir información de forma segura entre partes de manera firmada digitalmente, comúnmente utilizado para autenticación y autorización sin estado (stateless)."),
            Map.entry("oauth2", "Protocolo de autorización estándar de la industria que permite a aplicaciones de terceros obtener acceso limitado a cuentas de usuario sin exponer sus credenciales de acceso principales."),
            Map.entry("react", "Librería de JavaScript declarativa y basada en componentes para la construcción de interfaces de usuario interactivas, reactivas y eficientes para aplicaciones web de una sola página (SPA).")
    );


    private static final Map<String, List<String>> ADJACENCY_LIST = EDGES.stream()
            .flatMap(edge -> Stream.of(new String[]{edge.from, edge.to}, new String[]{edge.to, edge.from}))
            .collect(Collectors.groupingBy(
                    pair -> pair[0],
                    Collectors.mapping(pair -> pair[1], Collectors.toList())
            ));

    public static final Map<String, ConceptDetail> CONCEPTS = NODES.stream()
            .collect(Collectors.toMap(
                    node -> node.id,
                    node -> {
                        List<String> related = ADJACENCY_LIST.getOrDefault(node.id, List.of());
                        String description = CONCEPT_DESCRIPTIONS.getOrDefault(node.id, "Descripción de " + node.label);
                        return new ConceptDetail(
                                node.id,
                                node.label,
                                node.group,
                                description,
                                related.size(),
                                related,
                                // Contenido de ejemplo
                                List.of("Artículo sobre " + node.label, "Curso de " + node.label)
                        );
                    }
            ));

    public static String getCategoryColor(String category) {
        return CATEGORY_COLORS.getOrDefault(category, "#94A3B8");
    }

    public static String getCategoryBackgroundColor(String category) {
        return CATEGORY_BACKGROUND_COLORS.getOrDefault(category, "var(--lumo-contrast-10pct)");
    }

    private static NodeDto node(String id, String label, String group) {
        return new NodeDto(id, label, group, getCategoryColor(group));
    }
}
