package com.application.repository;

import com.application.model.Contenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContenidoRepository extends JpaRepository<Contenido, Long> {

    List<Contenido> findByUserIdOrderByFechaCreacionDesc(UUID userId);

    /**
     * Los "topK" contenidos con embedding mas parecido (coseno) al vector dado, ordenados de mas a
     * menos similar. embeddingLiteral es el formato de texto de pgvector, p.ej. "[0.01,-0.02,...]"
     * (ver EmbeddingVectors.toPgVectorLiteral). Se usa tanto para "relacionados" (post-guardado,
     * filtrando el propio id en el servicio) como para deteccion de casi-duplicados (pre-guardado).
     */
    @Query(value = """
            SELECT id, titulo, categoria, (1 - (embedding <=> CAST(:embedding AS vector))) AS similarity
            FROM public.contenido
            WHERE embedding IS NOT NULL
            ORDER BY embedding <=> CAST(:embedding AS vector) ASC
            LIMIT :topK
            """, nativeQuery = true)
    List<SimilarityMatch> findTopSimilar(@Param("embedding") String embeddingLiteral, @Param("topK") int topK);

    interface SimilarityMatch {
        Long getId();
        String getTitulo();
        String getCategoria();
        Double getSimilarity();
    }

    /** Solo título + categoría (sin texto/embedding), para armar el catálogo liviano que
     *  RagChatService le da al modelo como "de qué temas sé" — no confundir con findTopSimilar,
     *  que es la búsqueda semántica real para responder con contenido citado. */
    List<TituloCategoria> findAllBy();

    interface TituloCategoria {
        String getTitulo();
        String getCategoria();
    }
}
