package com.hackaton.logicore.entity;

import com.hackaton.logicore.enums.ResourceType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "resources")
@Data
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = true, length = 1000)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String texto;

    private String categoria;

    private String tecnologia;

    // --- NUEVO CAMPO: Tipo de Recurso ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType tipo;

    @Column(nullable = false)
    private Boolean publico = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}