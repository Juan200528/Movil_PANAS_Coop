package com.juan.movil_panas_coop.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ActividadModel {

    @SerializedName("_id")
    private String _id;

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

    // ✅ CAMBIO: Usar solo isPromoted (eliminar promocionada y estado)
    @SerializedName("isPromoted")
    private boolean isPromoted;

    // ✅ CAMBIO: Agregar campos del backend que faltaban
    @SerializedName("user")
    private String user;

    @SerializedName("asistentes")
    private List<String> asistentes;

    @SerializedName("promotion")
    private PromotionModel promotion;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Campos locales (no se envían al backend)
    private String imagePath;
    private boolean assisted;
    private boolean past;

    public ActividadModel() {}

    // ================================
    // GETTERS Y SETTERS COMPLETOS
    // ================================

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public List<String> getResponsible() {
        return responsible;
    }

    public void setResponsible(List<String> responsible) {
        this.responsible = responsible;
    }

    public boolean isPromoted() {
        return isPromoted;
    }

    public void setPromoted(boolean promoted) {
        isPromoted = promoted;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<String> getAsistentes() {
        return asistentes;
    }

    public void setAsistentes(List<String> asistentes) {
        this.asistentes = asistentes;
    }

    public PromotionModel getPromotion() {
        return promotion;
    }

    public void setPromotion(PromotionModel promotion) {
        this.promotion = promotion;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isAssisted() {
        return assisted;
    }

    public void setAssisted(boolean assisted) {
        this.assisted = assisted;
    }

    public boolean isPast() {
        return past;
    }

    public void setPast(boolean past) {
        this.past = past;
    }
}