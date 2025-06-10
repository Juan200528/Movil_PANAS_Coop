// Asegúrate que este archivo esté en: com/juan/movil_panas_coop/models/Comunidad.java
package com.juan.movil_panas_coop.model;

public class Comunidad {
    private String idMongo;
    private String nombre;
    private String descripcion;

    // Constructor vacío requerido para algunas librerías (e.g., Firestore)
    public Comunidad() {
    }

    public Comunidad(String idMongo, String nombre, String descripcion) {
        this.idMongo = idMongo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getIdMongo() {
        return idMongo;
    }

    public void setIdMongo(String idMongo) {
        this.idMongo = idMongo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}