package com.application.data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StaticConceptData {

    public record NodeDto(String id, String label, String group) {}
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

    public static final List<NodeDto> NODES = List.of(
            new NodeDto("spring-boot", "Spring Boot", "Backend"),
            new NodeDto("kafka", "Kafka", "Backend"),
            new NodeDto("jwt", "JWT", "Security"),
            new NodeDto("oauth2", "OAuth2", "Security"),
            new NodeDto("microservices", "Microservices", "Arquitectura"),
            new NodeDto("react", "React", "Frontend"),
            new NodeDto("typescript", "TypeScript", "Frontend"),
            new NodeDto("kubernetes", "Kubernetes", "DevOps"),
            new NodeDto("terraform", "Terraform", "DevOps"),
            new NodeDto("docker", "Docker", "DevOps"),
            new NodeDto("oci", "OCI", "Cloud"),
            new NodeDto("python", "Python", "Data Science"),
            new NodeDto("pandas", "Pandas", "Data Science")
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
}
