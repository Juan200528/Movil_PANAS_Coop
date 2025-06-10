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
        Log.d("CrearActividad", "pickImage() called");

        if (!storagePerm()) {
            Log.d("CrearActividad", "Storage permission not granted, requesting permission");
            String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
            ActivityCompat.requestPermissions(this, new String[]{perm}, PERM_REQ);
        } else {
            Log.d("CrearActividad", "Storage permission granted, opening gallery");
            openImageGallery();
        }
    }

    private void openImageGallery() {
        try {
            // Método preferido para Android moderno
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");

            // Verificar si hay una app que pueda manejar este intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, PICK_IMAGE);
                Log.d("CrearActividad", "Gallery intent started successfully");
            } else {
                // Fallback si no hay app de galería
                Log.w("CrearActividad", "No gallery app found, trying alternative");
                Intent fallbackIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fallbackIntent.setType("image/*");

                if (fallbackIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(fallbackIntent, PICK_IMAGE);
                } else {
                    Toast.makeText(this, "No se encontró una aplicación para seleccionar imágenes", Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            Log.e("CrearActividad", "Error opening image gallery", e);
            Toast.makeText(this, "Error al abrir la galería: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("CrearActividad", "onActivityResult - requestCode: " + requestCode +
                ", resultCode: " + resultCode + ", data: " + (data != null));

        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImageUri = data.getData();

                if (selectedImageUri != null) {
                    Log.d("CrearActividad", "Selected image URI: " + selectedImageUri.toString());

                    try {
                        // Mostrar la imagen en el ImageView
                        ivImg.setImageURI(selectedImageUri);
                        Log.d("CrearActividad", "Image displayed in ImageView successfully");

                        // Convertir URI a File en un hilo separado para evitar bloquear UI
                        new Thread(() -> {
                            File convertedFile = uriToFile(selectedImageUri);

                            runOnUiThread(() -> {
                                if (convertedFile != null) {
                                    imgFile = convertedFile;
                                    Log.d("CrearActividad", "Image conversion successful");
                                    Toast.makeText(CrearActividad.this, "Imagen seleccionada correctamente", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("CrearActividad", "Image conversion failed");
                                    Toast.makeText(CrearActividad.this, "Error al procesar la imagen seleccionada", Toast.LENGTH_SHORT).show();
                                    // Limpiar la imagen del ImageView si falló la conversión
                                    ivImg.setImageResource(R.drawable.ic_launcher_background); // o tu imagen por defecto
                                }
                            });
                        }).start();

                    } catch (Exception e) {
                        Log.e("CrearActividad", "Error in onActivityResult", e);
                        Toast.makeText(this, "Error al cargar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("CrearActividad", "Selected image URI is null");
                    Toast.makeText(this, "No se pudo obtener la imagen seleccionada", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d("CrearActividad", "Image selection cancelled or failed");
                Toast.makeText(this, "Selección de imagen cancelada", Toast.LENGTH_SHORT).show();
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
        if (uri == null) {
            Log.e("CrearActividad", "URI is null");
            return null;
        }

        Log.d("CrearActividad", "Processing URI: " + uri.toString());

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e("CrearActividad", "InputStream is null for URI: " + uri);
                return null;
            }

            // Crear directorio cache si no existe
            File cacheDir = getCacheDir();
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            File tempFile = new File(cacheDir, "temp_image_" + System.currentTimeMillis() + ".jpg");

            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096]; // Buffer más grande
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.flush();
                Log.d("CrearActividad", "File created successfully: " + tempFile.getAbsolutePath());
                Log.d("CrearActividad", "File size: " + tempFile.length() + " bytes");

                return tempFile;

            } finally {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Log.e("CrearActividad", "Error closing input stream", e);
                }
            }

        } catch (Exception e) {
            Log.e("CrearActividad", "Error converting URI to File", e);
            Toast.makeText(this, "Error al procesar la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
