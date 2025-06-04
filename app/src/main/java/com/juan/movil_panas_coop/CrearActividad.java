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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.juan.movil_panas_coop.api.ApiService;
import com.juan.movil_panas_coop.api.RetrofitClient;
import com.juan.movil_panas_coop.models.Actividad;
import com.juan.movil_panas_coop.utils.SessionManager;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearActividad extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 100;

    private EditText etTitulo, etDescripcion, etFecha, etLugar, etResponsables;
    private ImageButton btnSubir, btnCalendario;
    private ImageView ivActividadImagen;

    private File imagenFile = null;
    private Uri imagenUri = null;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_actividad);

        RetrofitClient.init(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        etLugar = findViewById(R.id.etLugar);
        etResponsables = findViewById(R.id.etResponsables);

        btnSubir = findViewById(R.id.btnSubir);
        btnCalendario = findViewById(R.id.btnCalendario);
        ivActividadImagen = findViewById(R.id.ivActividadImagen);

        findViewById(R.id.btnCrear).setOnClickListener(v -> crearActividad());

        btnSubir.setOnClickListener(v -> seleccionarImagen());
        btnCalendario.setOnClickListener(v -> mostrarDatePicker());
        etFecha.setOnClickListener(v -> mostrarDatePicker());
    }

    private boolean tienePermisoGaleria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void pedirPermisoGaleria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private void seleccionarImagen() {
        if (!tienePermisoGaleria()) {
            pedirPermisoGaleria();
        } else {
            abrirGaleria();
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();
        int year = calendario.get(Calendar.YEAR);
        int month = calendario.get(Calendar.MONTH);
        int day = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int y, int m, int d) -> {
                    String fechaSeleccionada = String.format("%04d-%02d-%02d", y, m + 1, d);
                    etFecha.setText(fechaSeleccionada);
                }, year, month, day);
        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imagenUri = data.getData();
            if (imagenUri != null) {
                ivActividadImagen.setImageURI(imagenUri);
                imagenFile = uriToFile(imagenUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria();
            } else {
                Toast.makeText(this, "Permiso denegado para acceder a la galería", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("temp_image", ".jpg", getCacheDir());
            tempFile.deleteOnExit();

            java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();

            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void crearActividad() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String lugar = etLugar.getText().toString().trim();
        String responsables = etResponsables.getText().toString().trim();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(fecha) ||
                TextUtils.isEmpty(lugar)) {
            Toast.makeText(this, "Complete todos los campos requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody reqTitulo = RequestBody.create(titulo, MediaType.parse("text/plain"));
        RequestBody reqDescripcion = RequestBody.create(descripcion, MediaType.parse("text/plain"));
        RequestBody reqFecha = RequestBody.create(fecha, MediaType.parse("text/plain"));
        RequestBody reqLugar = RequestBody.create(lugar, MediaType.parse("text/plain"));
        RequestBody reqResponsables = RequestBody.create(responsables, MediaType.parse("text/plain"));
        RequestBody reqEstado = RequestBody.create("pendiente", MediaType.parse("text/plain"));
        RequestBody reqPromocionada = RequestBody.create("false", MediaType.parse("text/plain"));

        MultipartBody.Part imagenPart = null;
        if (imagenFile != null && imagenFile.exists()) {
            RequestBody requestFile = RequestBody.create(imagenFile, MediaType.parse("image/*"));
            imagenPart = MultipartBody.Part.createFormData("imagen", imagenFile.getName(), requestFile);
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<Actividad> call = apiService.crearActividad(
                reqTitulo,
                reqDescripcion,
                reqFecha,
                reqLugar,
                reqResponsables,
                reqEstado,
                reqPromocionada,
                imagenPart
        );

        call.enqueue(new Callback<Actividad>() {
            @Override
            public void onResponse(Call<Actividad> call, Response<Actividad> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CrearActividad.this, "Actividad creada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String mensaje = "Error: ";
                    try {
                        mensaje += response.errorBody().string();
                    } catch (Exception e) {
                        mensaje += response.message();
                    }
                    Toast.makeText(CrearActividad.this, mensaje, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Actividad> call, Throwable t) {
                Toast.makeText(CrearActividad.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
