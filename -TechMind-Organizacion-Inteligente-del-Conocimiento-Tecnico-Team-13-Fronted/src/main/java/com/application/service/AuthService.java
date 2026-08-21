package com.application.service;

import com.application.model.User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final SupabaseAuthService supabaseAuthService;

    public AuthService(SupabaseAuthService supabaseAuthService) {
        this.supabaseAuthService = supabaseAuthService;
    }

    public User authenticate(String email, String password) {
        return supabaseAuthService.signIn(email, password);
    }

    public User register(String email, String password, String nombre) {
        System.out.println("Attempting to register user via Supabase. Email: " + email + ", Nombre: " + nombre);
        return supabaseAuthService.signUp(email, password, nombre);
    }

    public void logout() {
        supabaseAuthService.signOut();
    }
}
