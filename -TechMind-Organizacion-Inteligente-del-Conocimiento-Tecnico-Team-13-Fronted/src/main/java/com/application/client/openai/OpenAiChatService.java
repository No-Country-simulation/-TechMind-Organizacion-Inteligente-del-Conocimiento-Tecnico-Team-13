package com.application.client.openai;

import com.application.exception.ModeloServiceException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Chat completions de OpenAI para el Consultor IA (RAG multi-turno con citas). */
@Service
public class OpenAiChatService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiChatService.class);

    private final WebClient openAiWebClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OpenAiChatService(@Qualifier("openAiWebClient") WebClient openAiWebClient,
                              ObjectMapper objectMapper,
                              @Value("${openai.chat-model}") String model) {
        this.openAiWebClient = openAiWebClient;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    public String chat(List<ChatTurn> messages) {
        try {
            ChatResponse response = openAiWebClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(Map.of(
                            "model", model,
                            "messages", messages,
                            "temperature", 0.3
                    ))
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .block(Duration.ofSeconds(30));

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new ModeloServiceException("OpenAI no devolvió ninguna respuesta de chat");
            }
            return response.choices().get(0).message().content();
        } catch (ModeloServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ModeloServiceException("Error consultando el chat de OpenAI: " + e.getMessage(), e);
        }
    }

    /** Igual que {@link #chat}, pero pide la respuesta con "stream": true y llama a onToken con
     *  cada fragmento de texto a medida que llega (Server-Sent Events), en vez de esperar la
     *  respuesta completa. Devuelve el texto acumulado al terminar. */
    public String chatStreaming(List<ChatTurn> messages, Consumer<String> onToken) {
        long inicio = System.currentTimeMillis();
        StringBuilder acumulado = new StringBuilder();
        try {
            openAiWebClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(Map.of(
                            "model", model,
                            "messages", messages,
                            "temperature", 0.3,
                            "stream", true
                    ))
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                    })
                    .doOnNext(event -> {
                        String token = extraerTokenDelta(event.data());
                        if (token != null && !token.isEmpty()) {
                            acumulado.append(token);
                            onToken.accept(token);
                        }
                    })
                    .blockLast(Duration.ofSeconds(60));

            if (acumulado.isEmpty()) {
                log.error("Streaming de chat sin ningún token tras {} ms (la conexión SSE terminó vacía, sin error de red)", System.currentTimeMillis() - inicio);
                throw new ModeloServiceException("OpenAI no devolvió ninguna respuesta de chat (streaming)");
            }
            log.info("Streaming de chat completado: {} caracteres en {} ms", acumulado.length(), System.currentTimeMillis() - inicio);
            return acumulado.toString();
        } catch (ModeloServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Fallo el streaming de chat tras {} ms ({} caracteres acumulados antes de fallar): {}",
                    System.currentTimeMillis() - inicio, acumulado.length(), e.toString(), e);
            throw new ModeloServiceException("Error consultando el chat de OpenAI (streaming): " + e.getMessage(), e);
        }
    }

    /** Extrae choices[0].delta.content del chunk JSON de un evento SSE de OpenAI; null para el
     *  evento terminal "[DONE]" o si el chunk no trae texto (p.ej. solo metadata de rol). */
    private String extraerTokenDelta(String data) {
        if (data == null || "[DONE]".equals(data.trim())) {
            return null;
        }
        try {
            JsonNode choices = objectMapper.readTree(data).path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return null;
            }
            JsonNode content = choices.get(0).path("delta").path("content");
            return content.isMissingNode() ? null : content.asText();
        } catch (Exception e) {
            log.warn("No se pudo parsear un chunk de streaming de OpenAI: {}", e.getMessage());
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(ChatTurn message) {
    }
}
