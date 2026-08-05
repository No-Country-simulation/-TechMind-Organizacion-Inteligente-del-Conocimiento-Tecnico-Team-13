package com.application.service;

import com.application.model.AuthResponse;
import com.application.model.SupabaseUser;
import com.application.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseAuthService {

    private final RestTemplate restTemplate;
    private final UserSession userSession;
    private final String supabaseUrl;
    private final String supabaseApiKey;

    public SupabaseAuthService(RestTemplate restTemplate,
                               UserSession userSession,
                               @Value("${supabase.url}") String supabaseUrl,
                               @Value("${supabase.api-key}") String supabaseApiKey) {
        this.restTemplate = restTemplate;
        this.userSession = userSession;
        this.supabaseUrl = supabaseUrl;
        this.supabaseApiKey = supabaseApiKey;
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
                return user;
            }
            return null;
        } catch (HttpClientErrorException e) {
            // Log exception e.getResponseBodyAsString()
            return null;
        }
    }

    public User signUp(String email, String password, String nombre) {
        String url = supabaseUrl + "/auth/v1/signup";
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
            ResponseEntity<SupabaseUser> response = restTemplate.postForEntity(url, request, SupabaseUser.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                SupabaseUser supabaseUser = response.getBody();
                User user = new User();
                user.setId(UUID.fromString(supabaseUser.getId()));
                user.setEmail(supabaseUser.getEmail());
                user.setNombre(supabaseUser.getNombre());
                // Note: SignUp might not automatically sign in.
                // Depending on Supabase settings (e.g., email confirmation),
                // a separate signIn call might be needed.
                // For simplicity here, we'll consider the user signed in.
                userSession.setAuthenticatedUser(user);
                return user;
            }
            return null;
        } catch (HttpClientErrorException e) {
            // Log exception e.getResponseBodyAsString()
            return null;
        }
    }

    public void signOut() {
        // Here you would typically call Supabase's /auth/v1/logout endpoint
        // For simplicity, we just clear the local session
        userSession.clear();
    }
}
