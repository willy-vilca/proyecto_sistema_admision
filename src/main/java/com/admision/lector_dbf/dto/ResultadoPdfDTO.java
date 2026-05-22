package com.admision.lector_dbf.dto;

public class ResultadoPdfDTO {

    private Integer sec;
    private String codigo;
    private String nombre;
    private Double puntaje;
    private Integer merito;
    private String condicion;

    public ResultadoPdfDTO() {
    }

    public ResultadoPdfDTO(Integer sec, String codigo, String nombre, Double puntaje, Integer merito, String condicion) {
        this.sec = sec;
        this.codigo = codigo;
        this.nombre = nombre;
        this.puntaje = puntaje;
        this.merito = merito;
        this.condicion = condicion;
    }

    public Integer getSec() {
        return sec;
    }

    public void setSec(Integer sec) {
        this.sec = sec;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Double puntaje) {
        this.puntaje = puntaje;
    }

    public Integer getMerito() {
        return merito;
    }

    public void setMerito(Integer merito) {
        this.merito = merito;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }
}
