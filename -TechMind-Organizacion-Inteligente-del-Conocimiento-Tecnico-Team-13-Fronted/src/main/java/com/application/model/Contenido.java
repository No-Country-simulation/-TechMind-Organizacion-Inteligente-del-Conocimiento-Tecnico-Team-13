package com.application.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "contenido")
public class Contenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    private String categoria;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "contenido_palabras_clave", joinColumns = @JoinColumn(name = "contenido_id"))
    @Column(name = "palabra_clave")
    private List<String> palabrasClave = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "contenido_relacionados", joinColumns = @JoinColumn(name = "contenido_id"))
    @Column(name = "contenido_relacionado")
    private List<String> contenidosRelacionados = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    // Constructors
    public Contenido() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Contenido(String titulo, String texto, String categoria, List<String> palabrasClave, List<String> contenidosRelacionados) {
        this.titulo = titulo;
        this.texto = texto;
        this.categoria = categoria;
        this.palabrasClave = palabrasClave != null ? new ArrayList<>(palabrasClave) : new ArrayList<>();
        this.contenidosRelacionados = contenidosRelacionados != null ? new ArrayList<>(contenidosRelacionados) : new ArrayList<>();
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public List<String> getPalabrasClave() {
        return palabrasClave;
    }

    public void setPalabrasClave(List<String> palabrasClave) {
        this.palabrasClave = palabrasClave != null ? new ArrayList<>(palabrasClave) : new ArrayList<>();
    }

    public List<String> getContenidosRelacionados() {
        return contenidosRelacionados;
    }

    public void setContenidosRelacionados(List<String> contenidosRelacionados) {
        this.contenidosRelacionados = contenidosRelacionados != null ? new ArrayList<>(contenidosRelacionados) : new ArrayList<>();
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
