package com.application.model;

import java.util.UUID;

/**
 * Usuario autenticado, tal como lo devuelve la API REST de Supabase Auth (SupabaseAuthService).
 * No es una entidad JPA: nada lo persiste ni lo consulta vía repositorio, solo se arma en memoria
 * a partir de la respuesta de /auth/v1/token o /auth/v1/signup y se guarda en UserSession.
 */
public class User {

    private UUID id;
    private String email;
    private String password;
    private String nombre;

    public User() {
    }

    public User(String email, String password, String nombre) {
        this.email = email;
        this.password = password;
        this.nombre = nombre;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
