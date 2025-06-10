package com.juan.movil_panas_coop.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("id")
    private String id;  // Changed to String

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("telefono")
    private String phone;

    @SerializedName("direccion")
    private String address;

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}