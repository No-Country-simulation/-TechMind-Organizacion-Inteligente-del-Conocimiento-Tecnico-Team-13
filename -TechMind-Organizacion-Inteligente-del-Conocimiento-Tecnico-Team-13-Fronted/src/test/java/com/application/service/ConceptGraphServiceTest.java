package com.application.service;

import com.application.model.Contenido;
import com.application.repository.ContenidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConceptGraphServiceTest {

    @Mock
    private ContenidoRepository contenidoRepository;

    @Test
    void construyeNodosYAristasSinDuplicarConexionesBidireccionales() {
        ConceptGraphService service = new ConceptGraphService(contenidoRepository, 0.75);

        Contenido a = new Contenido();
        a.setId(1L);
        a.setTitulo("A");
        a.setCategoria("Backend");
        a.setEmbedding(new float[]{0.1f});

        Contenido b = new Contenido();
        b.setId(2L);
        b.setTitulo("B");
        b.setCategoria("Backend");
        b.setEmbedding(new float[]{0.1f});

        when(contenidoRepository.findAll()).thenReturn(List.of(a, b));

        ContenidoRepository.SimilarityMatch matchBDesdeA = mock(ContenidoRepository.SimilarityMatch.class);
        when(matchBDesdeA.getId()).thenReturn(2L);
        when(matchBDesdeA.getSimilarity()).thenReturn(0.9);

        ContenidoRepository.SimilarityMatch matchADesdeB = mock(ContenidoRepository.SimilarityMatch.class);
        when(matchADesdeB.getId()).thenReturn(1L);
        when(matchADesdeB.getSimilarity()).thenReturn(0.9);

        when(contenidoRepository.findTopSimilar(anyString(), anyInt()))
                .thenReturn(List.of(matchBDesdeA))
                .thenReturn(List.of(matchADesdeB));

        ConceptGraphService.Graph graph = service.build(4);

        assertThat(graph.nodos()).hasSize(2);
        // A-B y B-A deben colapsar en una sola arista, no dos.
        assertThat(graph.aristas()).hasSize(1);
    }

    @Test
    void ignoraContenidoSinEmbedding() {
        ConceptGraphService service = new ConceptGraphService(contenidoRepository, 0.75);

        Contenido sinEmbedding = new Contenido();
        sinEmbedding.setId(1L);
        sinEmbedding.setTitulo("Sin embedding");
        sinEmbedding.setCategoria("Backend");

        when(contenidoRepository.findAll()).thenReturn(List.of(sinEmbedding));

        ConceptGraphService.Graph graph = service.build(4);

        assertThat(graph.nodos()).hasSize(1);
        assertThat(graph.aristas()).isEmpty();
        verify(contenidoRepository, never()).findTopSimilar(anyString(), anyInt());
    }
}
