package com.juan.movil_panas_coop.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Actividad {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("date")
    private String date;

    @SerializedName("place")
    private String place;

    @SerializedName("responsible")
    private List<String> responsible;

    @SerializedName("estado")
    private String estado;

    @SerializedName("promocionada")
    private boolean promocionada;

    @SerializedName("pasada")
    private boolean pasada;

    @SerializedName("asistido")
    private boolean asistido;

    // Getters y setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setPlace(String place) { this.place = place; }
    public void setResponsible(List<String> responsible) { this.responsible = responsible; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setPromocionada(boolean promocionada) { this.promocionada = promocionada; }
    public void setPasada(boolean pasada) { this.pasada = pasada; }
    public void setAsistido(boolean asistido) { this.asistido = asistido; }
}
