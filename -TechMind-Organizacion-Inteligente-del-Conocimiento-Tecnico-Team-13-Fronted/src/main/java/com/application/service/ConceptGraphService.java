package com.application.service;

import com.application.model.Contenido;
import com.application.repository.ContenidoRepository;
import com.pgvector.PGvector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Arma el grafo de conceptos (nodos = Contenido, aristas = similitud coseno de sus embeddings) que
 * consume ConceptGraphView. Reusa los mismos embeddings ya calculados al guardar contenido (ver
 * ContenidoService), no genera nada nuevo con OpenAI.
 */
@Service
public class ConceptGraphService {

    private final ContenidoRepository contenidoRepository;
    private final double minSimilarity;

    public ConceptGraphService(ContenidoRepository contenidoRepository,
                                @Value("${rag.related-content.min-similarity}") double minSimilarity) {
        this.contenidoRepository = contenidoRepository;
        this.minSimilarity = minSimilarity;
    }

    public record Node(Long id, String titulo, String categoria) {
    }

    public record Edge(Long origenId, Long destinoId, double similitud) {
    }

    public record Graph(List<Node> nodos, List<Edge> aristas) {
    }

    public Graph build(int maxAristasPorNodo) {
        List<Contenido> todos = contenidoRepository.findAll();

        List<Node> nodos = todos.stream()
                .map(c -> new Node(c.getId(), c.getTitulo(), c.getCategoria() != null ? c.getCategoria() : "Sin categoría"))
                .collect(Collectors.toList());

        Set<String> aristasVistas = new HashSet<>();
        List<Edge> aristas = new ArrayList<>();
        for (Contenido c : todos) {
            if (c.getEmbedding() == null) {
                continue;
            }
            String literal = new PGvector(c.getEmbedding()).toString();
            contenidoRepository.findTopSimilar(literal, maxAristasPorNodo + 1).stream()
                    .filter(m -> !m.getId().equals(c.getId()))
                    .filter(m -> m.getSimilarity() != null && m.getSimilarity() >= minSimilarity)
                    .forEach(m -> {
                        String clave = c.getId() < m.getId() ? c.getId() + "-" + m.getId() : m.getId() + "-" + c.getId();
                        if (aristasVistas.add(clave)) {
                            aristas.add(new Edge(c.getId(), m.getId(), m.getSimilarity()));
                        }
                    });
        }
        return new Graph(nodos, aristas);
    }
}
