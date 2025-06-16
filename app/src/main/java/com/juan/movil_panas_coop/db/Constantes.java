package com.juan.movil_panas_coop.db;

public class Constantes {
    public static final String DATABASE_NAME = "panascoop_db";
    public static final int DATABASE_VERSION = 8; // Incrementado por nuevas columnas en notificacion

    // Tabla Usuarios
    public static final String TABLA_USUARIOS = "usuarios";
    public static final String COLUMNA_ID = "id";
    public static final String COLUMNA_NOMBRE = "nombre";
    public static final String COLUMNA_DIRECCION = "direccion";

    public static final String COLUMNA_TELEFONO = "telefono";
    public static final String COLUMNA_EMAIL = "email";
    public static final String COLUMNA_PASSWORD = "password";
    public static final String CREAR_TABLA_USUARIOS = "CREATE TABLE " + TABLA_USUARIOS + " (" +
            COLUMNA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMNA_NOMBRE + " TEXT, " +
            COLUMNA_EMAIL + " TEXT, " +
            COLUMNA_TELEFONO + " TEXT, " +
            COLUMNA_DIRECCION + " TEXT, " +
            COLUMNA_PASSWORD + " TEXT)";


    // Tabla Actividades
    public static final String TABLA_ACTIVIDADES = "actividades";
    public static final String COLUMNA_ID_ACTIVIDAD = "id_actividad";
    public static final String COLUMNA_TITULO = "titulo";
    public static final String COLUMNA_DESCRIPCION = "descripcion";
    public static final String COLUMNA_FECHA = "fecha";
    public static final String COLUMNA_LUGAR = "lugar";
    public static final String COLUMNA_ID_CREADOR = "id_creador";
    public static final String COLUMNA_RESPONSABLES = "responsables";
    public static final String COLUMNA_ESTADO = "estado";
    public static final String COLUMNA_IMAGEN_RUTA = "imagen_ruta";
    public static final String COLUMNA_PROMOCIONADA = "promocionada";
    public static final String CREAR_TABLA_ACTIVIDADES = "CREATE TABLE " + TABLA_ACTIVIDADES + " (" +
            COLUMNA_ID_ACTIVIDAD + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMNA_TITULO + " TEXT, " +
            COLUMNA_DESCRIPCION + " TEXT, " +
            COLUMNA_FECHA + " TEXT, " +
            COLUMNA_LUGAR + " TEXT, " +
            COLUMNA_ID_CREADOR + " INTEGER, " +
            COLUMNA_RESPONSABLES + " TEXT, " +
            COLUMNA_ESTADO + " TEXT, " +
            COLUMNA_IMAGEN_RUTA + " TEXT, " +
            COLUMNA_PROMOCIONADA + " INTEGER DEFAULT 0)";

    // Tabla Asistente
    public static final String TABLA_ASISTENTE = "asistente";
    public static final String CAMPO_ID = "id";
    public static final String CAMPO_ID_ASISTENTE = "id_asistente";
    public static final String CAMPO_NOMBRE_COMPLETO = "nombre_completo";
    public static final String CAMPO_CORREO = "correo";
    public static final String CAMPO_ACTIVIDAD_NOMBRE = "actividad_nombre";

    public static final String CREAR_TABLA_ASISTENTE = "CREATE TABLE " + TABLA_ASISTENTE + " (" +
            CAMPO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CAMPO_ID_ASISTENTE + " INTEGER, " +
            COLUMNA_ID_ACTIVIDAD + " INTEGER, " +
            CAMPO_NOMBRE_COMPLETO + " TEXT NOT NULL, " +
            CAMPO_CORREO + " TEXT NOT NULL, " +
            CAMPO_ACTIVIDAD_NOMBRE + " TEXT, " +
            "FOREIGN KEY (" + CAMPO_ID_ASISTENTE + ") REFERENCES " + TABLA_USUARIOS + "(" + COLUMNA_ID + "), " +
            "FOREIGN KEY (" + COLUMNA_ID_ACTIVIDAD + ") REFERENCES " + TABLA_ACTIVIDADES + "(" + COLUMNA_ID_ACTIVIDAD + "))";

    // Tabla Notificacion
    public static final String TABLA_NOTIFICACION = "notificacion";
    public static final String CAMPO_MENSAJE = "mensaje";
    public static final String CAMPO_FECHA = "fecha";
    public static final String CAMPO_ID_USUARIO = "id_usuario";
    public static final String CAMPO_DIAS = "dias";
    public static final String CAMPO_DIAS_RESTANTES = "dias_restantes";
    public static final String CAMPO_NOMBRE_ACTIVIDAD = "nombre_actividad";
    public static final String CAMPO_DIAS_ACTIVIDAD = "dias_actividad";

    public static final String CREAR_TABLA_NOTIFICACION = "CREATE TABLE " + TABLA_NOTIFICACION + " (" +
            CAMPO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CAMPO_MENSAJE + " TEXT, " +
            CAMPO_FECHA + " TEXT, " +
            CAMPO_ID_USUARIO + " INTEGER, " +
            CAMPO_DIAS + " INTEGER, " +
            CAMPO_DIAS_RESTANTES + " INTEGER, " +
            CAMPO_NOMBRE_ACTIVIDAD + " TEXT, " +
            CAMPO_DIAS_ACTIVIDAD + " INTEGER, " +
            COLUMNA_ID_ACTIVIDAD + " INTEGER, " +
            "FOREIGN KEY(" + CAMPO_ID_USUARIO + ") REFERENCES " + TABLA_USUARIOS + "(" + COLUMNA_ID + "), " +
            "FOREIGN KEY(" + COLUMNA_ID_ACTIVIDAD + ") REFERENCES " + TABLA_ACTIVIDADES + "(" + COLUMNA_ID_ACTIVIDAD + "))";
}