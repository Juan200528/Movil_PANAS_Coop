package com.juan.movil_panas_coop;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
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

        // Inicializar servicios y componentes
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

        // Configurar botón con efecto de estado presionado
        setupButtonWithStateEffect();

        btnDate.setOnClickListener(v -> showDatePicker());
        btnSubir.setOnClickListener(v -> pickImage());
        findViewById(R.id.btnCrear).setOnClickListener(v -> upload());
    }

    private void setupButtonWithStateEffect() {
        // Estado normal
        GradientDrawable gradientDrawableNormal = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#03683E"), Color.parseColor("#064349")});
        gradientDrawableNormal.setCornerRadius(80f);

        // Estado presionado
        GradientDrawable gradientDrawablePressed = new GradientDrawable();
        gradientDrawablePressed.setColor(Color.parseColor("#063449")); // Color al presionar
        gradientDrawablePressed.setCornerRadius(80f);

        // StateListDrawable para manejar los estados
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, gradientDrawablePressed);
        stateListDrawable.addState(new int[]{}, gradientDrawableNormal);

        // Asignar el drawable al botón
        findViewById(R.id.btnCrear).setBackground(stateListDrawable);
    }

    private void showToastAndLog(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e("CrearActividad", message);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) -> {
            etFecha.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }



    private void pickImage() {
        Log.d("CrearActividad", "pickImage() called - Using ACTION_PICK with chooser");

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        // Aseguramos que haya apps disponibles
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(
                    Intent.createChooser(intent, "Selecciona una imagen"),
                    PICK_IMAGE
            );
        } else {
            Toast.makeText(this, "No hay aplicaciones disponibles para seleccionar imágenes", Toast.LENGTH_SHORT).show();
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
                showToastAndLog("No se pudo procesar la imagen seleccionada");
            }
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

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(fecha) || TextUtils.isEmpty(lugar)) {
            showToastAndLog("Título, fecha, y lugar son obligatorios");
            return;
        }

        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            showToastAndLog("Token de autenticación faltante. Por favor inicia sesión.");
            return;
        }

        ActividadModel actividad = new ActividadModel();
        actividad.setTitle(titulo);
        actividad.setDescription(descripcion);
        actividad.setDate(formatDateForBackend(fecha));
        actividad.setPlace(lugar);
        if (!responsablesStr.isEmpty()) {
            List<String> responsablesList = Arrays.asList(responsablesStr.split("\\s*,\\s*"));
            actividad.setResponsible(responsablesList);
        }

        Call<ActividadModel> call = api.crearActividad("Bearer " + token, actividad);
        call.enqueue(new Callback<ActividadModel>() {
            @Override
            public void onResponse(Call<ActividadModel> call, Response<ActividadModel> response) {
                cleanupTempFile();
                if (response.isSuccessful()) {
                    showToastAndLog("Actividad creada exitosamente");
                    finish();
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ActividadModel> call, Throwable t) {
                cleanupTempFile();
                showToastAndLog("Error de conexión: " + t.getMessage());
            }
        });
    }

    private String formatDateForBackend(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr + "T00:00:00.000Z";
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
                switch (response.code()) {
                    case 400:
                        showToastAndLog("Datos inválidos. Revisa los campos.");
                        break;
                    case 401:
                        showToastAndLog("No autorizado. Inicia sesión nuevamente.");
                        break;
                    case 500:
                        showToastAndLog("Error del servidor. Intenta más tarde.");
                        break;
                    default:
                        showToastAndLog("Error: " + errorBody);
                }
            } else {
                showToastAndLog("Error desconocido del servidor");
            }
        } catch (Exception e) {
            showToastAndLog("Error procesando respuesta del servidor");
        }
    }
}