package com.application.service;

import com.application.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final SupabaseAuthService supabaseAuthService;

    public AuthService(SupabaseAuthService supabaseAuthService) {
        this.supabaseAuthService = supabaseAuthService;
    }

    public User authenticate(String email, String password) {
        log.info("Intento de login: email={}", email);
        return supabaseAuthService.signIn(email, password);
    }

    public User register(String email, String password, String nombre) {
        log.info("Intento de registro: email={}, nombre={}", email, nombre);
        return supabaseAuthService.signUp(email, password, nombre);
    }

    public void logout() {
        supabaseAuthService.signOut();
    }
}
