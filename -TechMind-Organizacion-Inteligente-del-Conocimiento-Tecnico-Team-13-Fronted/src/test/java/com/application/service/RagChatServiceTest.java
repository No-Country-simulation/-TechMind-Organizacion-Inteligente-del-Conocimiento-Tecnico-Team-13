package com.application.service;

import com.application.client.openai.ChatTurn;
import com.application.client.openai.OpenAiChatService;
import com.application.client.openai.OpenAiEmbeddingService;
import com.application.model.Contenido;
import com.application.repository.ContenidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagChatServiceTest {

    @Mock
    private ContenidoRepository contenidoRepository;
    @Mock
    private OpenAiEmbeddingService embeddingService;
    @Mock
    private OpenAiChatService chatService;

    @Test
    @SuppressWarnings("unchecked")
    void respondeConCitasCuandoHayContenidoRelevante() {
        RagChatService ragChatService = new RagChatService(contenidoRepository, embeddingService, chatService, 3, 0.75);

        when(embeddingService.embed("¿Qué es Kubernetes?")).thenReturn(new float[]{0.1f, 0.2f});

        ContenidoRepository.SimilarityMatch match = mock(ContenidoRepository.SimilarityMatch.class);
        when(match.getId()).thenReturn(10L);
        when(match.getSimilarity()).thenReturn(0.88);
        when(contenidoRepository.findTopSimilar(anyString(), anyInt())).thenReturn(List.of(match));

        Contenido contenido = new Contenido();
        contenido.setId(10L);
        contenido.setTitulo("Kubernetes Best Practices");
        contenido.setTexto("Kubernetes es un orquestador de contenedores...");
        when(contenidoRepository.findAllById(List.of(10L))).thenReturn(List.of(contenido));

        when(chatService.chat(anyList())).thenReturn("Kubernetes es un orquestador [Kubernetes Best Practices].");

        RagChatService.RagAnswer respuesta = ragChatService.ask("¿Qué es Kubernetes?", List.of());

        assertThat(respuesta.respuesta()).contains("orquestador");
        assertThat(respuesta.fuentes()).hasSize(1);
        assertThat(respuesta.fuentes().get(0).titulo()).isEqualTo("Kubernetes Best Practices");

        ArgumentCaptor<List<ChatTurn>> captor = ArgumentCaptor.forClass(List.class);
        verify(chatService).chat(captor.capture());
        List<ChatTurn> mensajes = captor.getValue();
        assertThat(mensajes.get(0).role()).isEqualTo("system");
        assertThat(mensajes.get(mensajes.size() - 1).content()).contains("Kubernetes Best Practices");
    }

    @Test
    @SuppressWarnings("unchecked")
    void avisaCuandoNoHayContenidoRelevante() {
        RagChatService ragChatService = new RagChatService(contenidoRepository, embeddingService, chatService, 3, 0.75);

        when(embeddingService.embed(anyString())).thenReturn(new float[]{0.1f});
        when(contenidoRepository.findTopSimilar(anyString(), anyInt())).thenReturn(List.of());
        when(chatService.chat(anyList())).thenReturn("No tengo información sobre esto en la base de conocimiento.");

        RagChatService.RagAnswer respuesta = ragChatService.ask("¿Algo muy random?", List.of());

        assertThat(respuesta.fuentes()).isEmpty();

        ArgumentCaptor<List<ChatTurn>> captor = ArgumentCaptor.forClass(List.class);
        verify(chatService).chat(captor.capture());
        assertThat(captor.getValue().get(captor.getValue().size() - 1).content())
                .contains("No se encontró contenido relevante");
    }
}
