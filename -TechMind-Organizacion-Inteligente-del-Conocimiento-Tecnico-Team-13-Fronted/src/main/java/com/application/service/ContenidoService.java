package com.application.service;

import com.application.client.ModeloClienteService;
import com.application.client.ModeloResponse;
import com.application.client.openai.OpenAiEmbeddingService;
import com.application.dto.ContenidoRelacionadoDTO;
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
        return procesarYGuardar(userId, request, tipoContenido, storagePath, null);
    }

    /**
     * @param categoriaUsuario si no es null/blank, se guarda como categoría en vez de la que
     *                         sugiera el clasificador (el usuario confirma/corrige en el diálogo
     *                         de AddContentView). Las palabras clave siempre vienen del clasificador.
     */
    public GuardadoResult procesarYGuardar(UUID userId, ContenidoRequestDTO request, String tipoContenido,
                                            String storagePath, String categoriaUsuario) {
        log.info("Procesando contenido: userId={}, titulo=\"{}\", tipo={}", userId, request.titulo(), tipoContenido);
        String categoria = null;
        Double probabilidad = null;
        List<String> palabrasClave = Collections.emptyList();
        try {
            ModeloResponse analisis = modeloClienteService.analizarContenido(request.titulo(), request.texto()).block();
            if (analisis != null) {
                categoria = analisis.categoria();
                probabilidad = analisis.probabilidad();
                palabrasClave = analisis.palabrasClave() != null ? analisis.palabrasClave() : Collections.emptyList();
            }
        } catch (ModeloServiceException e) {
            log.warn("Clasificador (FastAPI) no disponible, se guarda sin categoría/palabras clave: {}", e.getMessage());
        }

        if (categoriaUsuario != null && !categoriaUsuario.isBlank()) {
            categoria = categoriaUsuario.trim();
            // La probabilidad del clasificador ya no aplica: el usuario reemplazó su sugerencia.
            probabilidad = null;
        }

        float[] embedding = null;
        try {
            embedding = embeddingService.embed(request.titulo() + "\n" + request.texto());
        } catch (ModeloServiceException e) {
            log.warn("OpenAI embeddings no disponible, se guarda sin búsqueda semántica: {}", e.getMessage());
        }

        List<DuplicateWarning> duplicados = embedding != null
                ? buscarPosiblesDuplicados(userId, embedding)
                : Collections.emptyList();
        if (!duplicados.isEmpty()) {
            log.warn("Posibles duplicados detectados para \"{}\": {}", request.titulo(), duplicados);
        }

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
        log.info("Contenido guardado: id={}, titulo=\"{}\", categoria={}, embedding={}",
                guardado.getId(), guardado.getTitulo(), guardado.getCategoria(), embedding != null ? "ok" : "no disponible");

        List<ContenidoRelacionadoDTO> relacionados = embedding != null
                ? buscarRelacionados(userId, embedding, guardado.getId())
                : Collections.emptyList();

        ContenidoResponseDTO dto = new ContenidoResponseDTO(
                guardado.getId(),
                guardado.getTitulo(),
                guardado.getTexto(),
                guardado.getCategoria(),
                probabilidad,
                guardado.getPalabrasClave(),
                relacionados,
                guardado.getFechaCreacion().toLocalDateTime()
        );

        return new GuardadoResult(dto, duplicados);
    }

    public List<Contenido> listarPorUsuario(UUID userId) {
        return contenidoRepository.findByUserIdOrderByFechaCreacionDesc(userId);
    }

    /** Borra el contenido (si pertenece al usuario) y devuelve la entidad borrada, para que el
     *  llamador pueda limpiar también el archivo en Storage si tenía storagePath. */
    public Contenido eliminar(Long id, UUID userId) {
        Contenido contenido = contenidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contenido no encontrado: " + id));
        if (!contenido.getUserId().equals(userId)) {
            throw new IllegalArgumentException("No autorizado para eliminar este contenido");
        }
        contenidoRepository.delete(contenido);
        log.info("Contenido eliminado: id={}, titulo=\"{}\", userId={}", id, contenido.getTitulo(), userId);
        return contenido;
    }

    /** Cambia la categoría de un contenido (si pertenece al usuario) — usado desde CategoriesView
     *  para mover contenido entre carpetas y para renombrar una categoría completa (llamando esto
     *  una vez por cada contenido que la tenía). */
    public void actualizarCategoria(Long id, UUID userId, String nuevaCategoria) {
        Contenido contenido = contenidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contenido no encontrado: " + id));
        if (!contenido.getUserId().equals(userId)) {
            throw new IllegalArgumentException("No autorizado para modificar este contenido");
        }
        contenido.setCategoria(nuevaCategoria);
        contenidoRepository.save(contenido);
        log.info("Categoría actualizada: id={}, nuevaCategoria={}, userId={}", id, nuevaCategoria, userId);
    }

    private List<DuplicateWarning> buscarPosiblesDuplicados(UUID userId, float[] embedding) {
        String literal = new PGvector(embedding).toString();
        return contenidoRepository.findTopSimilarByUser(literal, userId, 5).stream()
                .filter(m -> m.getSimilarity() != null && m.getSimilarity() >= duplicateThreshold)
                .map(m -> new DuplicateWarning(m.getId(), m.getTitulo(), m.getSimilarity()))
                .collect(Collectors.toList());
    }

    private List<ContenidoRelacionadoDTO> buscarRelacionados(UUID userId, float[] embedding, Long excludeId) {
        String literal = new PGvector(embedding).toString();
        return contenidoRepository.findTopSimilarByUser(literal, userId, relatedTopK + 1).stream()
                .filter(m -> !m.getId().equals(excludeId))
                .filter(m -> m.getSimilarity() != null && m.getSimilarity() >= relatedMinSimilarity)
                .limit(relatedTopK)
                .map(m -> new ContenidoRelacionadoDTO(m.getTitulo(), m.getSimilarity()))
                .collect(Collectors.toList());
    }
}
