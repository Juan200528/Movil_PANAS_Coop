package com.juan.movil_panas_coop.model;

import com.google.gson.annotations.SerializedName;

public class Actividad {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String titulo;

    @SerializedName("description")
    private String descripcion;

    @SerializedName("date")
    private String fecha;

    @SerializedName("place")
    private String lugar;

    @SerializedName("responsables")
    private String responsables;

    @SerializedName("image")
    private String imagen; // URL o nombre de la imagen, si el backend lo devuelve

    @SerializedName("estado")
    private String estado;

    @SerializedName("promocionada")
    private boolean promocionada;

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getResponsables() {
        return responsables;
    }

    public void setResponsables(String responsables) {
        this.responsables = responsables;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isPromocionada() {
        return promocionada;
    }

    public void setPromocionada(boolean promocionada) {
        this.promocionada = promocionada;
    }
}
