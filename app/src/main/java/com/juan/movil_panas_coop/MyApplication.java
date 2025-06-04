package com.juan.movil_panas_coop; // Asegúrate que el paquete sea el correcto

import android.app.Application;
import android.util.Log; // Importa Log
import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication"; // Etiqueta para el Log

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MyApplication onCreate() - INICIO"); // Log al inicio
        FirebaseApp.initializeApp(this);
        Log.d(TAG, "MyApplication onCreate() - Firebase inicializado."); // Log después de inicializar
    }
}