package com.application.client.openai;

/** Un turno del chat RAG. role: "system" | "user" | "assistant". */
public record ChatTurn(String role, String content) {

    public static ChatTurn system(String content) {
        return new ChatTurn("system", content);
    }

    public static ChatTurn user(String content) {
        return new ChatTurn("user", content);
    }

    public static ChatTurn assistant(String content) {
        return new ChatTurn("assistant", content);
    }
}
