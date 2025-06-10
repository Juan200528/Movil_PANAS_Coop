package com.juan.movil_panas_coop;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.juan.movil_panas_coop.model.Comunidad;

public class CrearComunidadActivity extends AppCompatActivity {

    private EditText editTextNombreComunidad, editTextDescripcionComunidad;
    private Button buttonCrearComunidad;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_comunidad);

        editTextNombreComunidad = findViewById(R.id.editTextNombreComunidad);
        editTextDescripcionComunidad = findViewById(R.id.editTextDescripcionComunidad);
        buttonCrearComunidad = findViewById(R.id.buttonCrearComunidad);

        db = FirebaseFirestore.getInstance();

        buttonCrearComunidad.setOnClickListener(v -> crearComunidad());
    }

    private void crearComunidad() {
        String nombre = editTextNombreComunidad.getText().toString().trim();
        String descripcion = editTextDescripcionComunidad.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            editTextNombreComunidad.setError("Ingrese el nombre de la comunidad");
            return;
        }

        if (TextUtils.isEmpty(descripcion)) {
            editTextDescripcionComunidad.setError("Ingrese la descripción de la comunidad");
            return;
        }

        Comunidad nuevaComunidad = new Comunidad("id_generado", nombre, descripcion);

        db.collection("comunidades")
                .add(nuevaComunidad)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Comunidad creada exitosamente", Toast.LENGTH_SHORT).show();
                    finish(); // Regresar a la pantalla anterior
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al crear la comunidad", Toast.LENGTH_SHORT).show();
                });
    }
}