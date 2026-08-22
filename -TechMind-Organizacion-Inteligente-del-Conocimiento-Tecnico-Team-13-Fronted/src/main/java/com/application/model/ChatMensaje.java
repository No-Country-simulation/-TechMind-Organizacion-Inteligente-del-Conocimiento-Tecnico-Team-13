package com.application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_mensaje", schema = "public")
public class ChatMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(columnDefinition = "TEXT")
    private String citas;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    public ChatMensaje() {
        this.fechaCreacion = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getCitas() {
        return citas;
    }

    public void setCitas(String citas) {
        this.citas = citas;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
