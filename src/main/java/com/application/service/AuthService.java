package com.application.service;

import com.application.model.Role;
import com.application.model.RoleType;
import com.application.model.User;
import com.application.repository.RoleRepository;
import com.application.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
