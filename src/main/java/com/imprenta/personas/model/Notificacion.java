package com.imprenta.personas.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long id;

    @Column(name = "emisor_id", nullable = false, length = 50)
    private String emisorId;

    @Column(name = "receptor_id", nullable = false, length = 50)
    private String receptorId;

    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "leida", nullable = false)
    private Boolean leida;

    @Column(name = "creada_en", insertable = false, updatable = false)
    private LocalDateTime creadaEn;

    @Column(name = "leida_en")
    private LocalDateTime leidaEn;

    public Notificacion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmisorId() { return emisorId; }
    public void setEmisorId(String emisorId) { this.emisorId = emisorId; }
    public String getReceptorId() { return receptorId; }
    public void setReceptorId(String receptorId) { this.receptorId = receptorId; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public Boolean getLeida() { return leida; }
    public void setLeida(Boolean leida) { this.leida = leida; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
    public void setCreadaEn(LocalDateTime creadaEn) { this.creadaEn = creadaEn; }
    public LocalDateTime getLeidaEn() { return leidaEn; }
    public void setLeidaEn(LocalDateTime leidaEn) { this.leidaEn = leidaEn; }
}
