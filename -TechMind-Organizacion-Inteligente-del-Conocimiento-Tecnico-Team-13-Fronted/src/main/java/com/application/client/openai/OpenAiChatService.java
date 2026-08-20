package com.application.client.openai;

import com.application.exception.ModeloServiceException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Chat completions de OpenAI para el Consultor IA (RAG multi-turno con citas). */
@Service
public class OpenAiChatService {

    private final WebClient openAiWebClient;
    private final String model;

    public OpenAiChatService(@Qualifier("openAiWebClient") WebClient openAiWebClient,
                              @Value("${openai.chat-model}") String model) {
        this.openAiWebClient = openAiWebClient;
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(ChatTurn message) {
    }
}
