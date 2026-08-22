package com.application.model;

import com.application.model.support.TextArrayType;
import com.application.model.support.VectorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Tabla unica de contenido tecnico: reemplaza la antigua tabla "contenidos" (accedida por REST
 * desde SupabaseService) y la entidad "Contenido" original que nunca se conecto a ningun flujo.
 */
@Entity
@Table(name = "contenido", schema = "public")
public class Contenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "tipo_contenido", nullable = false, length = 50)
    private String tipoContenido = "texto_plano";

    @Column(name = "storage_path")
    private String storagePath;

    @Column(name = "estado_procesamiento", nullable = false, length = 30)
    private String estadoProcesamiento = "pendiente";

    @Column(length = 100)
    private String categoria;

    // UserType a medida (no @JdbcTypeCode(SqlTypes.ARRAY)): el ArrayJdbcType/ArrayJavaType
    // genérico de Hibernate 6.2.x tiene un bug conocido y falla el INSERT con
    // "Could not convert 'java.lang.String' to 'java.lang.Class' ... to unwrap". TextArrayType
    // arma el java.sql.Array a mano por JDBC puro, igual que VectorType hace con "vector(n)".
    @Type(TextArrayType.class)
    @Column(name = "palabras_clave", columnDefinition = "text[]")
    private String[] palabrasClave = new String[0];

    @Type(VectorType.class)
    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    public Contenido() {
        this.fechaCreacion = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public String getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(String tipoContenido) {
        this.tipoContenido = tipoContenido;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getEstadoProcesamiento() {
        return estadoProcesamiento;
    }

    public void setEstadoProcesamiento(String estadoProcesamiento) {
        this.estadoProcesamiento = estadoProcesamiento;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public List<String> getPalabrasClave() {
        return new ArrayList<>(Arrays.asList(palabrasClave));
    }

    public void setPalabrasClave(List<String> palabrasClave) {
        this.palabrasClave = palabrasClave != null ? palabrasClave.toArray(new String[0]) : new String[0];
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
