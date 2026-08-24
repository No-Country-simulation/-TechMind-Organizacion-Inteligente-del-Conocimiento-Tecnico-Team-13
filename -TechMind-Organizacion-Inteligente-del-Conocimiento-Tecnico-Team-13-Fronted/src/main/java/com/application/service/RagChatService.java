package com.application.service;

import com.application.client.openai.ChatTurn;
import com.application.client.openai.OpenAiChatService;
import com.application.client.openai.OpenAiEmbeddingService;
import com.application.model.Contenido;
import com.application.repository.ContenidoRepository;
import com.pgvector.PGvector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Backend del Consultor IA: recupera contenido relevante por similitud de embeddings (misma tabla
 * y umbral que las "relacionados" de ContenidoService) y lo inyecta como contexto citable en el
 * chat de OpenAI. Multi-turno: el historial de la conversación se pasa tal cual, sin el contexto
 * inyectado de turnos anteriores, para no duplicar tokens de contexto en cada vuelta.
 */
@Service
public class RagChatService {

    private static final Logger log = LoggerFactory.getLogger(RagChatService.class);

    private static final String SYSTEM_PROMPT = """
            Eres el Consultor IA de KnowBase, un asistente técnico conversacional. Cada mensaje del
            usuario trae dos bloques de información real (nunca los inventes tú, siempre te los paso yo):

            - BIBLIOTECA: catálogo de todo lo que el usuario tiene guardado (categorías y títulos).
              Es tu autoconocimiento de qué hay disponible, siempre presente, sin importar la pregunta.
            - CONTEXTO: los fragmentos de texto más relevantes para ESTA pregunta puntual, según
              similitud semántica. Puede venir vacío si nada calza lo suficiente.

            Antes de responder, distinguí de forma natural (no con reglas rígidas ni palabras clave,
            sino entendiendo la intención real) entre dos tipos de pregunta:

            1) Preguntas SOBRE VOS o sobre qué hay disponible: tu función, qué podés hacer, qué
               documentos/temas/categorías tenés a disposición, saludos, charla casual. Para estas,
               respondé de forma natural y conversacional usando BIBLIOTECA (nombrá categorías o
               títulos reales si ayuda) y contá brevemente qué hacés (RAG sobre la base de
               conocimiento del usuario, respondés citando fuentes reales). NO necesitás CONTEXTO
               para esto, y NO es aceptable responder "no tengo información" a un saludo o a "qué
               documentos tenés" — eso está en BIBLIOTECA, úsalo.

            2) Preguntas sobre CONTENIDO específico (qué dice tal documento, cómo se configura X,
               resume Y, qué me recomiendas sobre Z): para estas SÍ dependés estrictamente de
               CONTEXTO. Si CONTEXTO no alcanza para responder, decilo explícitamente en vez de
               inventar — acá sí aplica la regla estricta de no alucinar. Cuando uses un fragmento
               de CONTEXTO, citá el título exacto entre corchetes, por ejemplo:
               [Kubernetes Best Practices].

            Muchas preguntas son mixtas (p.ej. "qué sabés de autenticación" mezcla "qué tenés
            guardado" con "contame el contenido") — en ese caso combinalas: mencioná qué hay en
            BIBLIOTECA relacionado y, si CONTEXTO trae algo, resumilo y citalo también.

            GUARDARRIEL — SOS UN ASISTENTE SOBRE LA BIBLIOTECA DEL USUARIO, NO UN CHATBOT GENERAL:
            cuando CONTEXTO no tiene la respuesta a una pregunta de tipo (2), tu única respuesta
            válida es decir que no tenés esa información en la biblioteca del usuario — nunca la
            completes con tu propio conocimiento general, aunque lo tengas y aunque el tema sea
            técnico. Esto aplica siempre, sin excepción: recetas de cocina, explicar qué es
            Kubernetes o AWS en general, sugerir herramientas que no estén en CONTEXTO/BIBLIOTECA,
            adivinar con qué tecnología se construyó esta app, dar consejos de carrera genéricos,
            "buscar en medios externos" (no podés, no tenés acceso a internet). Nada de eso sale de
            la biblioteca del usuario, así que no es aceptable responderlo, ni siquiera como
            "información adicional" o "por si te sirve" después del disclaimer. Patrón PROHIBIDO
            (NO hagas esto): "No tengo información específica sobre X en tu biblioteca. Sin embargo,
            puedo ofrecerte/contarte/explicarte..." seguido de la respuesta igual. Patrón CORRECTO:
            decir que no está en su biblioteca y, si ayuda, sugerir con qué SÍ cuenta (usando
            BIBLIOTECA) que se le acerque o que podría guardar para tener eso cubierto — sin
            desarrollar el tema en sí. Ejemplo: pregunta "dame una receta de pasta a la boloñesa" →
            responder algo como "No tengo información sobre recetas de cocina en tu biblioteca. Puedo
            ayudarte con lo que sí tenés guardado ahí — por ejemplo tus documentos de [categoría/
            título real de BIBLIOTECA]." y parar ahí, sin dar la receta.

            Responde en español, de forma breve, técnica, directa y natural — como una charla, no
            como un formulario rígido.

            Tu respuesta se renderiza como Markdown (encabezados, listas, **negritas**, código con
            ```lenguaje```). Úsalo cuando ayude a la claridad. Si una pregunta se responde mejor con
            un diagrama, una demo interactiva o una tabla visual en vez de solo texto, puedes incluir
            un bloque ```html``` con HTML/CSS/JS autocontenido: se renderiza como un mini "canvas" en
            un iframe aislado debajo de tu respuesta. Úsalo solo cuando de verdad aporte (ej. explicar
            un flujo con un diagrama, mostrar una comparación interactiva), no en cada respuesta.
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

    public RagAnswer ask(UUID userId, String pregunta, List<ChatTurn> historial) {
        return ask(userId, pregunta, historial, etapa -> { });
    }

    /** @param onStage callback informativo (para la UI: "generando embedding", "buscando...",
     *                 "generando respuesta"); no bloqueante, se puede pasar un no-op. */
    public RagAnswer ask(UUID userId, String pregunta, List<ChatTurn> historial, Consumer<String> onStage) {
        Recuperacion recuperacion = recuperarContexto(userId, pregunta, onStage);
        List<ChatTurn> mensajes = construirMensajes(userId, pregunta, historial, recuperacion.contextoTexto());

        onStage.accept("Generando respuesta…");
        String respuesta = chatService.chat(mensajes);
        log.info("Respuesta RAG generada: {} caracteres, {} cita(s)", respuesta.length(), recuperacion.citas().size());
        return new RagAnswer(respuesta, recuperacion.citas());
    }

    /** Igual que {@link #ask}, pero la respuesta se transmite token a token vía onToken en vez de
     *  esperar el texto completo (ver OpenAiChatService#chatStreaming). */
    public RagAnswer askStreaming(UUID userId, String pregunta, List<ChatTurn> historial, Consumer<String> onStage, Consumer<String> onToken) {
        Recuperacion recuperacion = recuperarContexto(userId, pregunta, onStage);
        List<ChatTurn> mensajes = construirMensajes(userId, pregunta, historial, recuperacion.contextoTexto());

        onStage.accept("Generando respuesta…");
        String respuesta = chatService.chatStreaming(mensajes, onToken);
        log.info("Respuesta RAG (streaming) generada: {} caracteres, {} cita(s)", respuesta.length(), recuperacion.citas().size());
        return new RagAnswer(respuesta, recuperacion.citas());
    }

    private record Recuperacion(String contextoTexto, List<Citation> citas) {
    }

    private Recuperacion recuperarContexto(UUID userId, String pregunta, Consumer<String> onStage) {
        log.info("Consulta RAG recibida: userId={}, \"{}\"", userId, pregunta);
        onStage.accept("Generando embedding de la pregunta…");
        float[] embedding = embeddingService.embed(pregunta);
        String literal = new PGvector(embedding).toString();

        onStage.accept("Buscando contenido relacionado en la base de conocimiento…");
        List<ContenidoRepository.SimilarityMatch> candidatos = contenidoRepository.findTopSimilarByUser(literal, userId, topK);
        // Detalle candidato por candidato: es lo que hace visible POR QUÉ algo entró o no al
        // contexto, en vez de solo el conteo final o el mejor score.
        candidatos.forEach(m -> log.info("  candidato: \"{}\" (id={}) similitud={} {} umbral {}",
                m.getTitulo(), m.getId(), m.getSimilarity(),
                m.getSimilarity() != null && m.getSimilarity() >= minSimilarity ? ">=" : "<", minSimilarity));

        List<ContenidoRepository.SimilarityMatch> matches = candidatos.stream()
                .filter(m -> m.getSimilarity() != null && m.getSimilarity() >= minSimilarity)
                .collect(Collectors.toList());
        log.info("Contexto RAG: {}/{} candidato(s) superan el umbral ({})", matches.size(), candidatos.size(), minSimilarity);

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
        return new Recuperacion(contextoTexto, citas);
    }

    private List<ChatTurn> construirMensajes(UUID userId, String pregunta, List<ChatTurn> historial, String contextoTexto) {
        List<ChatTurn> mensajes = new ArrayList<>();
        mensajes.add(ChatTurn.system(SYSTEM_PROMPT));
        mensajes.addAll(historial);
        mensajes.add(ChatTurn.user("BIBLIOTECA:\n" + construirCatalogoBiblioteca(userId)
                + "\n\nCONTEXTO:\n" + contextoTexto + "\n\nPREGUNTA: " + pregunta));
        return mensajes;
    }

    /** Catálogo liviano (categoría + título, sin texto) de lo que el usuario tiene guardado, para
     *  que el modelo pueda responder preguntas sobre sí mismo/qué tiene disponible sin necesitar
     *  que la búsqueda semántica encuentre nada — ver findByUserId() en ContenidoRepository. Se
     *  recalcula en cada pregunta (consulta liviana) para reflejar contenido agregado durante la
     *  conversación, y nunca incluye contenido de otros usuarios. */
    private String construirCatalogoBiblioteca(UUID userId) {
        List<ContenidoRepository.TituloCategoria> items = contenidoRepository.findByUserId(userId);
        if (items.isEmpty()) {
            return "(Todavía no hay contenido guardado en la base de conocimiento.)";
        }

        Map<String, List<String>> porCategoria = items.stream().collect(Collectors.groupingBy(
                i -> i.getCategoria() != null && !i.getCategoria().isBlank() ? i.getCategoria() : "Sin categoría",
                LinkedHashMap::new, Collectors.mapping(ContenidoRepository.TituloCategoria::getTitulo, Collectors.toList())));

        StringBuilder sb = new StringBuilder();
        sb.append(items.size()).append(" documento(s) guardado(s), por categoría:\n");
        porCategoria.forEach((categoria, titulos) -> {
            sb.append("- ").append(categoria).append(" (").append(titulos.size()).append("): ");
            List<String> muestra = titulos.size() > 15 ? titulos.subList(0, 15) : titulos;
            sb.append(String.join(", ", muestra));
            if (titulos.size() > muestra.size()) {
                sb.append(", … y ").append(titulos.size() - muestra.size()).append(" más");
            }
            sb.append('\n');
        });
        return sb.toString();
    }
}
