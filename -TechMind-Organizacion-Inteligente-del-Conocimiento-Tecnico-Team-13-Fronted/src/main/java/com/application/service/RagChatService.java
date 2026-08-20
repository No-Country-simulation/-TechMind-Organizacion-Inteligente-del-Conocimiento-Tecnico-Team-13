package com.application.service;

import com.application.client.openai.ChatTurn;
import com.application.client.openai.OpenAiChatService;
import com.application.client.openai.OpenAiEmbeddingService;
import com.application.model.Contenido;
import com.application.repository.ContenidoRepository;
import com.pgvector.PGvector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Backend del Consultor IA: recupera contenido relevante por similitud de embeddings (misma tabla
 * y umbral que las "relacionados" de ContenidoService) y lo inyecta como contexto citable en el
 * chat de OpenAI. Multi-turno: el historial de la conversación se pasa tal cual, sin el contexto
 * inyectado de turnos anteriores, para no duplicar tokens de contexto en cada vuelta.
 */
@Service
public class RagChatService {

    private static final String SYSTEM_PROMPT = """
            Eres el Consultor IA de KnowBase, un asistente técnico. Respondes SOLO usando el
            CONTEXTO de la base de conocimiento que se te entrega en cada mensaje del usuario.
            Si el contexto no alcanza para responder, dilo explícitamente en vez de inventar.
            Cuando uses un fragmento del contexto, cita el título exacto de la fuente entre
            corchetes, por ejemplo: [Kubernetes Best Practices]. Responde en español, de forma
            breve, técnica y directa.
            """;

    private final ContenidoRepository contenidoRepository;
    private final OpenAiEmbeddingService embeddingService;
    private final OpenAiChatService chatService;
    private final int topK;
    private final double minSimilarity;

    public RagChatService(ContenidoRepository contenidoRepository,
                           OpenAiEmbeddingService embeddingService,
                           OpenAiChatService chatService,
                           @Value("${rag.related-content.top-k}") int topK,
                           @Value("${rag.related-content.min-similarity}") double minSimilarity) {
        this.contenidoRepository = contenidoRepository;
        this.embeddingService = embeddingService;
        this.chatService = chatService;
        this.topK = topK;
        this.minSimilarity = minSimilarity;
    }

    public record Citation(Long id, String titulo, double similitud) {
    }

    public record RagAnswer(String respuesta, List<Citation> fuentes) {
    }

    public RagAnswer ask(String pregunta, List<ChatTurn> historial) {
        float[] embedding = embeddingService.embed(pregunta);
        String literal = new PGvector(embedding).toString();

        List<ContenidoRepository.SimilarityMatch> matches = contenidoRepository.findTopSimilar(literal, topK).stream()
                .filter(m -> m.getSimilarity() != null && m.getSimilarity() >= minSimilarity)
                .collect(Collectors.toList());

        Map<Long, Contenido> porId = matches.isEmpty()
                ? Map.of()
                : contenidoRepository.findAllById(matches.stream().map(ContenidoRepository.SimilarityMatch::getId).toList())
                        .stream().collect(Collectors.toMap(Contenido::getId, c -> c));

        StringBuilder contexto = new StringBuilder();
        List<Citation> citas = new ArrayList<>();
        for (ContenidoRepository.SimilarityMatch match : matches) {
            Contenido contenido = porId.get(match.getId());
            if (contenido == null) {
                continue;
            }
            String texto = contenido.getTexto();
            String snippet = texto != null && texto.length() > 800 ? texto.substring(0, 800) + "…" : texto;
            contexto.append("### ").append(contenido.getTitulo()).append('\n').append(snippet).append("\n\n");
            citas.add(new Citation(contenido.getId(), contenido.getTitulo(), match.getSimilarity()));
        }

        String contextoTexto = contexto.isEmpty()
                ? "(No se encontró contenido relevante en la base de conocimiento para esta pregunta.)"
                : contexto.toString();

        List<ChatTurn> mensajes = new ArrayList<>();
        mensajes.add(ChatTurn.system(SYSTEM_PROMPT));
        mensajes.addAll(historial);
        mensajes.add(ChatTurn.user("CONTEXTO:\n" + contextoTexto + "\n\nPREGUNTA: " + pregunta));

        String respuesta = chatService.chat(mensajes);
        return new RagAnswer(respuesta, citas);
    }
}
