package com.juan.movil_panas_coop.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context) {
        super(context, Constantes.DATABASE_NAME, null, Constantes.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constantes.CREAR_TABLA_USUARIOS);
        db.execSQL(Constantes.CREAR_TABLA_ACTIVIDADES);
        db.execSQL(Constantes.CREAR_TABLA_ASISTENTE);
        db.execSQL(Constantes.CREAR_TABLA_NOTIFICACION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            db.execSQL("DROP TABLE IF EXISTS " + Constantes.TABLA_ASISTENTE);
            db.execSQL(Constantes.CREAR_TABLA_ASISTENTE);
            db.execSQL("DROP TABLE IF EXISTS asistir");
        }
    }
}