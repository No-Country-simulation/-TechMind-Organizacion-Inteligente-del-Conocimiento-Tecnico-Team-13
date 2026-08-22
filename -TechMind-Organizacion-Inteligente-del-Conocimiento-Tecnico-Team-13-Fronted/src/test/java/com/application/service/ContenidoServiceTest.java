package com.application.service;

import com.application.client.ModeloClienteService;
import com.application.client.ModeloResponse;
import com.application.client.openai.OpenAiEmbeddingService;
import com.application.dto.ContenidoRequestDTO;
import com.application.exception.ModeloServiceException;
import com.application.model.Contenido;
import com.application.repository.ContenidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContenidoServiceTest {

    @Mock
    private ContenidoRepository contenidoRepository;
    @Mock
    private ModeloClienteService modeloClienteService;
    @Mock
    private OpenAiEmbeddingService embeddingService;

    private ContenidoService contenidoService;

    @BeforeEach
    void setUp() {
        contenidoService = new ContenidoService(contenidoRepository, modeloClienteService, embeddingService,
                5, 0.75, 0.90);
    }

    @Test
    void guardaConCategoriaYEmbeddingCuandoTodoFunciona() {
        UUID userId = UUID.randomUUID();
        ContenidoRequestDTO request = new ContenidoRequestDTO("Titulo", "Texto de prueba");

        when(modeloClienteService.analizarContenido("Titulo", "Texto de prueba"))
                .thenReturn(Mono.just(new ModeloResponse("Backend", 0.9, List.of("java", "spring"), List.of())));

        float[] embedding = new float[]{0.1f, 0.2f};
        when(embeddingService.embed(anyString())).thenReturn(embedding);
        when(contenidoRepository.findTopSimilar(anyString(), anyInt())).thenReturn(List.of());

        Contenido guardado = new Contenido();
        guardado.setId(1L);
        guardado.setTitulo("Titulo");
        guardado.setTexto("Texto de prueba");
        guardado.setCategoria("Backend");
        guardado.setPalabrasClave(List.of("java", "spring"));
        when(contenidoRepository.save(any(Contenido.class))).thenReturn(guardado);

        ContenidoService.GuardadoResult resultado = contenidoService.procesarYGuardar(userId, request, "texto_plano", null);

        assertThat(resultado.contenido().categoria()).isEqualTo("Backend");
        assertThat(resultado.contenido().palabrasClave()).containsExactly("java", "spring");
        assertThat(resultado.posiblesDuplicados()).isEmpty();

        ArgumentCaptor<Contenido> captor = ArgumentCaptor.forClass(Contenido.class);
        verify(contenidoRepository).save(captor.capture());
        assertThat(captor.getValue().getEstadoProcesamiento()).isEqualTo("completado");
        assertThat(captor.getValue().getEmbedding()).isEqualTo(embedding);
    }

    @Test
    void degradaSinCategoriaCuandoElClasificadorFalla() {
        UUID userId = UUID.randomUUID();
        ContenidoRequestDTO request = new ContenidoRequestDTO("Titulo", "Texto");

        when(modeloClienteService.analizarContenido(anyString(), anyString()))
                .thenReturn(Mono.error(new ModeloServiceException("FastAPI caído")));
        when(embeddingService.embed(anyString())).thenReturn(new float[]{0.1f});
        when(contenidoRepository.findTopSimilar(anyString(), anyInt())).thenReturn(List.of());

        Contenido guardado = new Contenido();
        guardado.setId(2L);
        guardado.setTitulo("Titulo");
        guardado.setTexto("Texto");
        when(contenidoRepository.save(any(Contenido.class))).thenReturn(guardado);

        ContenidoService.GuardadoResult resultado = contenidoService.procesarYGuardar(userId, request, "texto_plano", null);

        assertThat(resultado.contenido().categoria()).isNull();
        assertThat(resultado.contenido().palabrasClave()).isEmpty();

        ArgumentCaptor<Contenido> captor = ArgumentCaptor.forClass(Contenido.class);
        verify(contenidoRepository).save(captor.capture());
        assertThat(captor.getValue().getEstadoProcesamiento()).isEqualTo("sin_clasificar");
    }

    @Test
    void guardaSinEmbeddingCuandoOpenAiFalla() {
        UUID userId = UUID.randomUUID();
        ContenidoRequestDTO request = new ContenidoRequestDTO("Titulo", "Texto");

        when(modeloClienteService.analizarContenido(anyString(), anyString()))
                .thenReturn(Mono.just(new ModeloResponse("Backend", 0.8, List.of(), List.of())));
        when(embeddingService.embed(anyString())).thenThrow(new ModeloServiceException("OpenAI caído"));

        Contenido guardado = new Contenido();
        guardado.setId(3L);
        guardado.setTitulo("Titulo");
        guardado.setTexto("Texto");
        guardado.setCategoria("Backend");
        when(contenidoRepository.save(any(Contenido.class))).thenReturn(guardado);

        ContenidoService.GuardadoResult resultado = contenidoService.procesarYGuardar(userId, request, "texto_plano", null);

        assertThat(resultado.contenido().contenidosRelacionados()).isEmpty();
        assertThat(resultado.posiblesDuplicados()).isEmpty();
        verify(contenidoRepository, never()).findTopSimilar(anyString(), anyInt());
    }

    @Test
    void reportaCasiDuplicadosPorEncimaDelUmbralSinBloquearElGuardado() {
        UUID userId = UUID.randomUUID();
        ContenidoRequestDTO request = new ContenidoRequestDTO("Titulo", "Texto");

        when(modeloClienteService.analizarContenido(anyString(), anyString()))
                .thenReturn(Mono.just(new ModeloResponse("Backend", 0.8, List.of(), List.of())));
        when(embeddingService.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});

        ContenidoRepository.SimilarityMatch match = mock(ContenidoRepository.SimilarityMatch.class);
        when(match.getId()).thenReturn(99L);
        when(match.getTitulo()).thenReturn("Contenido parecido");
        when(match.getSimilarity()).thenReturn(0.95);
        when(contenidoRepository.findTopSimilar(anyString(), anyInt())).thenReturn(List.of(match));

        Contenido guardado = new Contenido();
        guardado.setId(4L);
        guardado.setTitulo("Titulo");
        guardado.setTexto("Texto");
        guardado.setCategoria("Backend");
        when(contenidoRepository.save(any(Contenido.class))).thenReturn(guardado);

        ContenidoService.GuardadoResult resultado = contenidoService.procesarYGuardar(userId, request, "texto_plano", null);

        assertThat(resultado.posiblesDuplicados()).hasSize(1);
        assertThat(resultado.posiblesDuplicados().get(0).titulo()).isEqualTo("Contenido parecido");
        // El guardado ocurre igual: los duplicados solo advierten, nunca bloquean.
        verify(contenidoRepository).save(any(Contenido.class));
    }
}
