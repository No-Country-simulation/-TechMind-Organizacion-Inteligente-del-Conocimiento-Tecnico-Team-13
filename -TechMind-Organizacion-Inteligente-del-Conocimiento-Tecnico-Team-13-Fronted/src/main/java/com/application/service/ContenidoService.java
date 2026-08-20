package com.application.service;

import com.application.client.ModeloClienteService;
import com.application.client.ModeloResponse;
import com.application.client.openai.OpenAiEmbeddingService;
import com.application.dto.ContenidoRequestDTO;
import com.application.dto.ContenidoResponseDTO;
import com.application.exception.ModeloServiceException;
import com.application.model.Contenido;
import com.application.repository.ContenidoRepository;
import com.pgvector.PGvector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Orquestador del Sprint 3 (POST /contenido): valida, clasifica (FastAPI), genera embedding
 * (OpenAI), detecta casi-duplicados, persiste y calcula contenido relacionado por similitud
 * coseno. La clasificación y el embedding degradan a "no disponible" en vez de bloquear el
 * guardado si el servicio externo correspondiente falla (resiliencia, ver Sprint 2).
 */
@Service
public class ContenidoService {

    private static final Logger log = LoggerFactory.getLogger(ContenidoService.class);

    private final ContenidoRepository contenidoRepository;
    private final ModeloClienteService modeloClienteService;
    private final OpenAiEmbeddingService embeddingService;
    private final int relatedTopK;
    private final double relatedMinSimilarity;
    private final double duplicateThreshold;

    public ContenidoService(ContenidoRepository contenidoRepository,
                             ModeloClienteService modeloClienteService,
                             OpenAiEmbeddingService embeddingService,
                             @Value("${rag.related-content.top-k}") int relatedTopK,
                             @Value("${rag.related-content.min-similarity}") double relatedMinSimilarity,
                             @Value("${rag.duplicate.warning-threshold}") double duplicateThreshold) {
        this.contenidoRepository = contenidoRepository;
        this.modeloClienteService = modeloClienteService;
        this.embeddingService = embeddingService;
        this.relatedTopK = relatedTopK;
        this.relatedMinSimilarity = relatedMinSimilarity;
        this.duplicateThreshold = duplicateThreshold;
    }

    public record GuardadoResult(ContenidoResponseDTO contenido, List<DuplicateWarning> posiblesDuplicados) {
    }

    public record DuplicateWarning(Long id, String titulo, double similitud) {
    }

    public GuardadoResult procesarYGuardar(UUID userId, ContenidoRequestDTO request, String tipoContenido, String storagePath) {
        String categoria = null;
        List<String> palabrasClave = Collections.emptyList();
        try {
            ModeloResponse analisis = modeloClienteService.analizarContenido(request.titulo(), request.texto()).block();
            if (analisis != null) {
                categoria = analisis.categoria();
                palabrasClave = analisis.palabrasClave() != null ? analisis.palabrasClave() : Collections.emptyList();
            }
        } catch (ModeloServiceException e) {
            log.warn("Clasificador (FastAPI) no disponible, se guarda sin categoría/palabras clave: {}", e.getMessage());
        }

        float[] embedding = null;
        try {
            embedding = embeddingService.embed(request.titulo() + "\n" + request.texto());
        } catch (ModeloServiceException e) {
            log.warn("OpenAI embeddings no disponible, se guarda sin búsqueda semántica: {}", e.getMessage());
        }

        List<DuplicateWarning> duplicados = embedding != null
                ? buscarPosiblesDuplicados(embedding)
                : Collections.emptyList();

        Contenido contenido = new Contenido();
        contenido.setUserId(userId);
        contenido.setTitulo(request.titulo());
        contenido.setTexto(request.texto());
        contenido.setTipoContenido(tipoContenido != null ? tipoContenido : "texto_plano");
        contenido.setStoragePath(storagePath);
        contenido.setEstadoProcesamiento(categoria != null ? "completado" : "sin_clasificar");
        contenido.setCategoria(categoria);
        contenido.setPalabrasClave(palabrasClave);
        contenido.setEmbedding(embedding);

        Contenido guardado = contenidoRepository.save(contenido);

        List<String> relacionados = embedding != null
                ? buscarRelacionados(embedding, guardado.getId())
                : Collections.emptyList();

        ContenidoResponseDTO dto = new ContenidoResponseDTO(
                guardado.getId(),
                guardado.getTitulo(),
                guardado.getTexto(),
                guardado.getCategoria(),
                guardado.getPalabrasClave(),
                relacionados,
                guardado.getFechaCreacion().toLocalDateTime()
        );

        return new GuardadoResult(dto, duplicados);
    }

    public List<Contenido> listarPorUsuario(UUID userId) {
        return contenidoRepository.findByUserIdOrderByFechaCreacionDesc(userId);
    }

    private List<DuplicateWarning> buscarPosiblesDuplicados(float[] embedding) {
        String literal = new PGvector(embedding).toString();
        return contenidoRepository.findTopSimilar(literal, 5).stream()
                .filter(m -> m.getSimilarity() != null && m.getSimilarity() >= duplicateThreshold)
                .map(m -> new DuplicateWarning(m.getId(), m.getTitulo(), m.getSimilarity()))
                .collect(Collectors.toList());
    }

    private List<String> buscarRelacionados(float[] embedding, Long excludeId) {
        String literal = new PGvector(embedding).toString();
        return contenidoRepository.findTopSimilar(literal, relatedTopK + 1).stream()
                .filter(m -> !m.getId().equals(excludeId))
                .filter(m -> m.getSimilarity() != null && m.getSimilarity() >= relatedMinSimilarity)
                .limit(relatedTopK)
                .map(ContenidoRepository.SimilarityMatch::getTitulo)
                .collect(Collectors.toList());
    }
}
