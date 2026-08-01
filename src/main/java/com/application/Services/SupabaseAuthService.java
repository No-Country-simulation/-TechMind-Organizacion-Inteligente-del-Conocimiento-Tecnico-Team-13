package com.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SupabaseAuthService {

    private final HttpClient client;
    private final URI signUpUri;
    private final URI signInUri;
    private final String apiKey;

    public SupabaseAuthService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.api-key}") String apiKey) {
        this.client = HttpClient.newHttpClient();
        this.signUpUri = URI.create(supabaseUrl + "/auth/v1/signup");
        this.signInUri = URI.create(supabaseUrl + "/auth/v1/token?grant_type=password");
        this.apiKey = apiKey;
    }

    public boolean signUp(String email, String password) {
        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        return sendRequest(signUpUri, body) != null;
    }

    public AuthenticatedUser signIn(String email, String password) {
        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        String responseBody = sendRequest(signInUri, body);
        if (responseBody == null) {
            return null;
        }
        return parseUserFromResponse(responseBody, email);
    }

    private String sendRequest(URI uri, String body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("apikey", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return response.body();
            }
        } catch (Exception ex) {
            // Ignored intentionally; login will return null on failure.
        }
        return null;
    }

    private AuthenticatedUser parseUserFromResponse(String body, String emailFallback) {
        String userJson = extractJsonObject(body, "\"user\"");
        String email = extractJsonValue(userJson, "email");
        if (email == null || email.isBlank()) {
            email = emailFallback;
        }

        String fullName = extractJsonValue(userJson, "full_name");
        if (fullName == null || fullName.isBlank()) {
            fullName = deriveNameFromEmail(email);
        }
        return new AuthenticatedUser(email, fullName);
    }

    private String extractJsonObject(String json, String key) {
        if (json == null || key == null) {
            return json;
        }
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) {
            return json;
        }
        int braceIndex = json.indexOf('{', keyIndex + key.length());
        if (braceIndex < 0) {
            return json;
        }
        int depth = 1;
        int index = braceIndex + 1;
        while (index < json.length() && depth > 0) {
            char c = json.charAt(index++);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
            }
        }
        return depth == 0 ? json.substring(braceIndex, index) : json;
    }

    private String extractJsonValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex < 0) {
            return null;
        }
        int colonIndex = json.indexOf(':', keyIndex + search.length());
        if (colonIndex < -1) {
            return null;
        }
        int quoteIndex = json.indexOf('"', colonIndex);
        if (quoteIndex < 0 || quoteIndex + 1 >= json.length()) {
            return null;
        }
        int endIndex = json.indexOf('"', quoteIndex + 1);
        if (endIndex < 0) {
            return null;
        }
        return json.substring(quoteIndex + 1, endIndex);
    }

    private String deriveNameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "Usuario";
        }
        String namePart = email.substring(0, email.indexOf('@'));
        return namePart.replaceAll("[^A-Za-z0-9]", " ").trim();
    }
}
