package com.admision.lector_dbf.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evidencias_anulacion")
public class EvidenciaAnulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombreOriginal;
    private String nombreArchivo;
    private String rutaArchivo;
    private LocalDateTime fechaSubida;

    @ManyToOne
    @JoinColumn(name = "examen_id")
    private Examen examen;

    public EvidenciaAnulacion() {
    }

    public Long getId() {
        return id;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public Examen getExamen() {
        return examen;
    }

    public void setExamen(Examen examen) {
        this.examen = examen;
    }
}
