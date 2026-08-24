package com.application.service;

import com.application.model.Contenido;
import com.application.repository.ContenidoRepository;
import com.pgvector.PGvector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Arma el grafo de conceptos (nodos = Contenido, aristas = similitud coseno de sus embeddings) que
 * consume ConceptGraphView. Reusa los mismos embeddings ya calculados al guardar contenido (ver
 * ContenidoService), no genera nada nuevo con OpenAI.
 */
@Service
public class ConceptGraphService {

    private static final Logger log = LoggerFactory.getLogger(ConceptGraphService.class);

    private final ContenidoRepository contenidoRepository;
    private final double minSimilarity;

    public ConceptGraphService(ContenidoRepository contenidoRepository,
                                @Value("${rag.related-content.min-similarity}") double minSimilarity) {
        this.contenidoRepository = contenidoRepository;
        this.minSimilarity = minSimilarity;
    }

    public record Node(Long id, String titulo, String categoria, String resumen) {
    }

    public record Edge(Long origenId, Long destinoId, double similitud) {
    }

    public record Graph(List<Node> nodos, List<Edge> aristas) {
    }

    /** El grafo se calcula solo con el contenido del usuario dado — nunca mezcla contenido de
     *  otros usuarios, ni como nodo ni como candidato de similitud. */
    public Graph build(UUID userId, int maxAristasPorNodo) {
        List<Contenido> todos = contenidoRepository.findByUserIdOrderByFechaCreacionDesc(userId);

        List<Node> nodos = todos.stream()
                .map(c -> new Node(c.getId(), c.getTitulo(),
                        c.getCategoria() != null ? c.getCategoria() : "Sin categoría",
                        resumen(c.getTexto())))
                .collect(Collectors.toList());

        Set<String> aristasVistas = new HashSet<>();
        List<Edge> aristas = new ArrayList<>();
        for (Contenido c : todos) {
            if (c.getEmbedding() == null) {
                continue;
            }
            String literal = new PGvector(c.getEmbedding()).toString();
            contenidoRepository.findTopSimilarByUser(literal, userId, maxAristasPorNodo + 1).stream()
                    .filter(m -> !m.getId().equals(c.getId()))
                    .filter(m -> m.getSimilarity() != null && m.getSimilarity() >= minSimilarity)
                    .forEach(m -> {
                        String clave = c.getId() < m.getId() ? c.getId() + "-" + m.getId() : m.getId() + "-" + c.getId();
                        if (aristasVistas.add(clave)) {
                            aristas.add(new Edge(c.getId(), m.getId(), m.getSimilarity()));
                        }
                    });
        }
        log.info("Grafo de conceptos construido: userId={}, {} nodo(s), {} arista(s)", userId, nodos.size(), aristas.size());
        return new Graph(nodos, aristas);
    }

    private String resumen(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        String normalizado = texto.replaceAll("\\s+", " ").trim();
        return normalizado.length() > 220 ? normalizado.substring(0, 220) + "…" : normalizado;
    }
}
