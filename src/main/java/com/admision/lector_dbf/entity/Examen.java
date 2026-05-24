package com.admision.lector_dbf.entity;

import jakarta.persistence.*;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "examenes")
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String litho;

    private String tema;

    private Double puntaje;

    private Integer ordenMerito;

    private Boolean ingreso;

    private Integer correctas;

    private Integer incorrectas;

    private Integer blancas;

    private Boolean anulado;

    @Column(columnDefinition = "TEXT")
    private String motivoAnulacion;

    @Column(columnDefinition = "TEXT")
    private String respuestasJson;

    private LocalDateTime fechaProceso;

    @ManyToOne
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @ManyToOne
    @JoinColumn(name = "proceso_id")
    private ProcesoAdmision procesoAdmision;

    @OneToMany(
            mappedBy = "examen",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<EvidenciaAnulacion> evidencias = new ArrayList<>();

    public Examen() {
    }

    @PrePersist
    public void prePersist() {
        fechaProceso = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getLitho() {
        return litho;
    }

    public void setLitho(String litho) {
        this.litho = litho;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public Double getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Double puntaje) {
        this.puntaje = puntaje;
    }

    public Integer getCorrectas() {
        return correctas;
    }

    public void setCorrectas(Integer correctas) {
        this.correctas = correctas;
    }

    public Integer getIncorrectas() {
        return incorrectas;
    }

    public void setIncorrectas(Integer incorrectas) {
        this.incorrectas = incorrectas;
    }

    public Integer getBlancas() {
        return blancas;
    }

    public void setBlancas(Integer blancas) {
        this.blancas = blancas;
    }

    public Boolean getAnulado() {
        return anulado;
    }

    public void setAnulado(Boolean anulado) {
        this.anulado = anulado;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public String getRespuestasJson() {
        return respuestasJson;
    }

    public void setRespuestasJson(String respuestasJson) {
        this.respuestasJson = respuestasJson;
    }

    public LocalDateTime getFechaProceso() {
        return fechaProceso;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public ProcesoAdmision getProcesoAdmision() {
        return procesoAdmision;
    }

    public void setProcesoAdmision(
            ProcesoAdmision procesoAdmision
    ) {
        this.procesoAdmision = procesoAdmision;
    }

    public Integer getOrdenMerito() {
        return ordenMerito;
    }

    public void setOrdenMerito(Integer ordenMerito) {
        this.ordenMerito = ordenMerito;
    }

    public Boolean getIngreso() {
        return ingreso;
    }

    public void setIngreso(Boolean ingreso) {
        this.ingreso = ingreso;
    }

    public List<EvidenciaAnulacion> getEvidencias() {
        return evidencias;
    }

    public void setEvidencias(List<EvidenciaAnulacion> evidencias) {
        this.evidencias = evidencias;
    }
}
