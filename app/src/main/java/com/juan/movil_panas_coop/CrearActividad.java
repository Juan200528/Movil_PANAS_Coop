package com.juan.movil_panas_coop;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.juan.movil_panas_coop.api.ApiService;
import com.juan.movil_panas_coop.api.RetrofitClient;
import com.juan.movil_panas_coop.model.ActividadModel;
import com.juan.movil_panas_coop.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearActividad extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PERM_REQ = 100;

    private EditText etTitulo, etDesc, etFecha, etLugar, etResp;
    private ImageButton btnSubir, btnDate;
    private ImageView ivImg;
    private File imgFile;
    private ApiService api;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_actividad);

        api = RetrofitClient.getApiService();
        sessionManager = new SessionManager(this);

        etTitulo = findViewById(R.id.etTitulo);
        etDesc = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        etLugar = findViewById(R.id.etLugar);
        etResp = findViewById(R.id.etResponsables);
        btnSubir = findViewById(R.id.btnSubir);
        btnDate = findViewById(R.id.btnCalendario);
        ivImg = findViewById(R.id.ivActividadImagen);

        btnDate.setOnClickListener(v -> showDatePicker());
        btnSubir.setOnClickListener(v -> pickImage());
        findViewById(R.id.btnCrear).setOnClickListener(v -> upload());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) -> {
            etFecha.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean storagePerm() {
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED;
    }

    private void pickImage() {
        if (!storagePerm()) {
            String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
            ActivityCompat.requestPermissions(this, new String[]{perm}, PERM_REQ);
        } else {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            startActivityForResult(i, PICK_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            ivImg.setImageURI(uri);
            imgFile = uriToFile(uri);
            if (imgFile == null) {
                Toast.makeText(this, "No se pudo procesar la imagen seleccionada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQ && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        } else {
            Toast.makeText(this, "Permiso para acceder a imágenes denegado", Toast.LENGTH_SHORT).show();
        }
    }

    private File uriToFile(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) return null;
            File tmp = new File(getCacheDir(), "img_" + UUID.randomUUID() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
            return tmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void upload() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDesc.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String lugar = etLugar.getText().toString().trim();
        String responsablesStr = etResp.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(fecha) || TextUtils.isEmpty(lugar)) {
            Toast.makeText(this, "Título, fecha, y lugar son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token de autenticación faltante. Por favor inicia sesión.", Toast.LENGTH_LONG).show();
            return;
        }

        // ✅ CREAR MODELO CORRECTAMENTE
        ActividadModel actividad = new ActividadModel();
        actividad.setTitle(titulo);
        actividad.setDescription(descripcion);

        // ✅ MEJOR MANEJO DE FECHA
        actividad.setDate(formatDateForBackend(fecha));
        actividad.setPlace(lugar);

        // ✅ MANEJO DE RESPONSABLES
        if (!responsablesStr.isEmpty()) {
            List<String> responsablesList = Arrays.asList(responsablesStr.split("\\s*,\\s*"));
            actividad.setResponsible(responsablesList);
        }

        // ✅ LLAMADA DIRECTA CON JSON (SIN MULTIPART)
        Call<ActividadModel> call = api.crearActividad("Bearer " + token, actividad);
        call.enqueue(new Callback<ActividadModel>() {
            @Override
            public void onResponse(Call<ActividadModel> call, Response<ActividadModel> response) {
                // ✅ LIMPIAR ARCHIVO TEMPORAL
                cleanupTempFile();

                if (response.isSuccessful()) {
                    Toast.makeText(CrearActividad.this, "Actividad creada exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ActividadModel> call, Throwable t) {
                // ✅ LIMPIAR ARCHIVO TEMPORAL EN ERROR
                cleanupTempFile();
                Toast.makeText(CrearActividad.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

// ✅ AGREGAR ESTOS MÉTODOS HELPER EN CrearActividad.java:

    private String formatDateForBackend(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr + "T00:00:00.000Z"; // fallback
        }
    }

    private void cleanupTempFile() {
        if (imgFile != null && imgFile.exists()) {
            boolean deleted = imgFile.delete();
            if (!deleted) {
                Log.w("CrearActividad", "No se pudo eliminar archivo temporal: " + imgFile.getAbsolutePath());
            }
        }
    }

    private void handleErrorResponse(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();

                // Manejo específico por código de error
                switch (response.code()) {
                    case 400:
                        Toast.makeText(this, "Datos inválidos. Revisa los campos.", Toast.LENGTH_LONG).show();
                        break;
                    case 401:
                        Toast.makeText(this, "No autorizado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
                        // Opcional: redirigir a login
                        break;
                    case 500:
                        Toast.makeText(this, "Error del servidor. Intenta más tarde.", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(this, "Error: " + errorBody, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Error desconocido del servidor", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error procesando respuesta del servidor", Toast.LENGTH_LONG).show();
        }
    }
}
