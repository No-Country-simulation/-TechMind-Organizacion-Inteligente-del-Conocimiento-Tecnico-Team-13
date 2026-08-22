package com.application.service;

import com.application.exception.EmailNotConfirmedException;
import com.application.exception.SupabaseAuthException;
import com.application.exception.SupabaseRateLimitException;
import com.application.model.AuthResponse;
import com.application.model.SupabaseUser;
import com.application.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseAuthService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseAuthService.class);

    private final RestTemplate restTemplate;
    private final UserSession userSession;
    private final ObjectMapper objectMapper;
    private final String supabaseUrl;
    private final String supabaseApiKey;
    private final String appBaseUrl;

    public SupabaseAuthService(RestTemplate restTemplate,
                               UserSession userSession,
                               ObjectMapper objectMapper,
                               @Value("${supabase.url}") String supabaseUrl,
                               @Value("${supabase.api-key}") String supabaseApiKey,
                               @Value("${app.base-url}") String appBaseUrl) {
        this.restTemplate = restTemplate;
        this.userSession = userSession;
        this.objectMapper = objectMapper;
        this.supabaseUrl = supabaseUrl;
        this.supabaseApiKey = supabaseApiKey;
        this.appBaseUrl = appBaseUrl;
    }

    public User signIn(String email, String password) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=password";
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, request, AuthResponse.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                SupabaseUser supabaseUser = response.getBody().getUser();
                User user = new User();
                user.setId(UUID.fromString(supabaseUser.getId()));
                user.setEmail(supabaseUser.getEmail());
                user.setNombre(supabaseUser.getNombre());
                // Store user in session
                userSession.setAuthenticatedUser(user);
                log.info("Login exitoso: userId={}, email={}", user.getId(), email);
                return user;
            }
            log.warn("Login fallido (respuesta inesperada de Supabase Auth): email={}, status={}", email, response.getStatusCode());
            return null;
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            if (errorBody != null && (errorBody.contains("email_not_confirmed") || errorBody.toLowerCase().contains("not confirmed"))) {
                log.warn("Login rechazado: correo no confirmado, email={}", email);
                throw new EmailNotConfirmedException("Debes confirmar tu correo antes de iniciar sesión.");
            }
            log.warn("Login fallido (credenciales inválidas o error de Supabase Auth): email={}, status={}", email, e.getStatusCode());
            return null;
        }
    }

    public User signUp(String email, String password, String nombre) {
        String url = UriComponentsBuilder.fromHttpUrl(supabaseUrl + "/auth/v1/signup")
                .queryParam("redirect_to", appBaseUrl + "/?confirmed=true")
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Map<String, String> data = new HashMap<>();
        data.put("nombre", nombre);
        body.put("data", data);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            // Supabase returns the user object at the root when confirmation is
            // pending, or wrapped under "user" (alongside a session) when the
            // project has email auto-confirm enabled. Parse both shapes.
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (response.getStatusCode() != HttpStatus.OK || responseBody == null) {
                return null;
            }

            Object userNode = responseBody.containsKey("user") ? responseBody.get("user") : responseBody;
            SupabaseUser supabaseUser = objectMapper.convertValue(userNode, SupabaseUser.class);
            if (supabaseUser == null || supabaseUser.getId() == null) {
                return null;
            }

            User user = new User();
            user.setId(UUID.fromString(supabaseUser.getId()));
            user.setEmail(supabaseUser.getEmail());
            user.setNombre(supabaseUser.getNombre());

            // Only auto-login if Supabase actually issued a session
            // (i.e. email confirmation is not required).
            if (responseBody.get("access_token") != null) {
                userSession.setAuthenticatedUser(user);
            }
            log.info("Registro exitoso: userId={}, email={}, autoLogin={}", user.getId(), email, responseBody.get("access_token") != null);
            return user;
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.warn("Supabase signUp error [{}]: {}", e.getStatusCode(), errorBody);
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS
                    || (errorBody != null && errorBody.toLowerCase().contains("rate limit"))) {
                throw new SupabaseRateLimitException("Se alcanzó el límite de correos de confirmación. Espera unos minutos y vuelve a intentar.");
            }
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new SupabaseAuthException("No se pudo conectar con Supabase (configuración de API key inválida). Contacta al administrador.");
            }
            return null;
        }
    }

    public void signOut() {
        // Here you would typically call Supabase's /auth/v1/logout endpoint
        // For simplicity, we just clear the local session
        log.info("Logout: userId={}", userSession.getAuthenticatedUser() != null ? userSession.getAuthenticatedUser().getId() : null);
        userSession.clear();
    }
}
