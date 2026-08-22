package com.application;

import com.application.client.ModeloClienteService;
import com.application.client.ModeloResponse;
import com.application.client.openai.OpenAiEmbeddingService;
import com.application.repository.ContenidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración real: Postgres + pgvector vía Testcontainers, la migración Flyway
 * (V1__enable_pgvector_and_create_contenido.sql) corre tal cual correría contra Supabase, y el
 * endpoint POST /contenido persiste + calcula similitud con SQL nativo real (no mockeado).
 *
 * Requiere Docker disponible en la máquina/CI donde se ejecuten los tests. No se llegó a correr
 * en el entorno donde se escribió (sin Docker disponible) — correrla localmente para verificar.
 *
 * Nombre corto (ContenidoApiIT en el paquete raíz) a propósito: la ruta de este repo ya es larga
 * y Windows tiene un límite de ~260 caracteres por archivo; un paquete/nombre más profundo rompe
 * "git add" con "Filename too long".
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ContenidoApiIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"))
            .withInitScript("test-init/auth-schema-stub.sql");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContenidoRepository contenidoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private ModeloClienteService modeloClienteService;

    @MockBean
    private OpenAiEmbeddingService embeddingService;

    private UUID userId;

    @BeforeEach
    void crearUsuarioDePrueba() {
        userId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO auth.users (id, email) VALUES (?, ?)", userId, "test@example.com");
    }

    @Test
    void guardaContenidoYQuedaDisponibleParaBusquedaPorSimilitud() throws Exception {
        when(modeloClienteService.analizarContenido(anyString(), anyString()))
                .thenReturn(Mono.just(new ModeloResponse("Backend", 0.9, List.of("java", "spring"), List.of())));
        when(embeddingService.embed(anyString())).thenReturn(embeddingDeEjemplo(0.1f));

        mockMvc.perform(post("/contenido")
                        .param("userId", userId.toString())
                        .contentType("application/json")
                        .content("{\"titulo\":\"Spring Boot Basics\",\"texto\":\"Spring Boot simplifica la configuración de apps Java.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contenido.categoria").value("Backend"))
                .andExpect(jsonPath("$.contenido.palabrasClave[0]").value("java"));

        assertThat(contenidoRepository.findByUserIdOrderByFechaCreacionDesc(userId)).hasSize(1);

        // Un segundo contenido con embedding casi idéntico debe salir como "posible duplicado".
        when(embeddingService.embed(anyString())).thenReturn(embeddingDeEjemplo(0.1000001f));

        mockMvc.perform(post("/contenido")
                        .param("userId", userId.toString())
                        .contentType("application/json")
                        .content("{\"titulo\":\"Spring Boot Basics (copia)\",\"texto\":\"Spring Boot simplifica la configuración de apps Java, otra vez.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.posiblesDuplicados[0].titulo").value("Spring Boot Basics"));
    }

    private float[] embeddingDeEjemplo(float base) {
        float[] v = new float[1536];
        for (int i = 0; i < v.length; i++) {
            v[i] = base;
        }
        return v;
    }
}
