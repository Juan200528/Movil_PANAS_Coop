package com.juan.movil_panas_coop.models;

public class Asistente {
    private int id;
    private String nombre; // Primer nombre para la UI
    private String nombreCompleto; // Nombre completo para la base de datos
    private String correo;
    private int idActividad;
    private int idAsistente;
    private String actividadNombre; // Nuevo campo para el nombre de la actividad

    // Constructor vacío
    public Asistente() {}

    // Constructor completo
    public Asistente(int id, String nombre, String nombreCompleto, String correo, int idActividad, int idAsistente, String actividadNombre) {
        this.id = id;
        this.nombre = nombre;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.idActividad = idActividad;
        this.idAsistente = idAsistente;
        this.actividadNombre = actividadNombre;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCorreoAbreviado() {
        if (correo == null || !correo.contains("@")) {
            return correo != null ? correo : "";
        }
        String[] partes = correo.split("@");
        String usuario = partes[0];
        String dominio = "@" + partes[1];
        if (usuario.length() > 6) {
            usuario = usuario.substring(0, 6) + "...";
        }
        return usuario + dominio;
    }

    public int getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(int idActividad) {
        this.idActividad = idActividad;
    }

    public int getIdAsistente() {
        return idAsistente;
    }

    public void setIdAsistente(int idAsistente) {
        this.idAsistente = idAsistente;
    }

    public String getActividadNombre() {
        return actividadNombre;
    }

    public void setActividadNombre(String actividadNombre) {
        this.actividadNombre = actividadNombre;
    }
}