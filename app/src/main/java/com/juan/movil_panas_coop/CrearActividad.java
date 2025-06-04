package com.juan.movil_panas_coop;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.juan.movil_panas_coop.db.ManagerDb;
import com.juan.movil_panas_coop.models.Actividad;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CrearActividad extends AppCompatActivity {
    private static final String TAG = "CrearActividad";
    private static final int TARGET_WIDTH_DP = 84; // Ancho fijo del contenedor
    private static final int TARGET_HEIGHT_DP = 56; // Alto fijo del contenedor
    private static final int IMAGE_RADIUS_DP = 16; // Radio de redondeo
    private static final int REQUEST_CODE_PERMISSIONS = 1001;

    private EditText etTitulo, etDescripcion, etFecha, etLugar, etResponsables, etImage;
    private ImageButton btnCalendario, btnSubir;
    private Button btnCrear;
    private ImageView ivActividadImagen;
    private Calendar calendario = Calendar.getInstance();
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private ManagerDb managerDb;
    private SharedPreferences sharedPreferences;
    private String imagenRuta;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_actividad);

        inicializarVistas();
        configurarBaseDatos();
        configurarImagePicker();
        configurarBotones();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (managerDb != null) {
            managerDb.close();
        }
    }

    private void inicializarVistas() {
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        etLugar = findViewById(R.id.etLugar);
        etResponsables = findViewById(R.id.etResponsables);
        etImage = findViewById(R.id.etImage);
        btnCalendario = findViewById(R.id.btnCalendario);
        btnSubir = findViewById(R.id.btnSubir);
        btnCrear = findViewById(R.id.btnCrear);
        ivActividadImagen = findViewById(R.id.ivActividadImagen);
        ivActividadImagen.setImageResource(R.drawable.default_image);
    }

    private void configurarBaseDatos() {
        managerDb = new ManagerDb(this);
        managerDb.open(); // Abrir conexión a base de datos
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
    }

    private void configurarImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap processedBitmap = procesarImagen(imageUri);
                            ivActividadImagen.setImageBitmap(processedBitmap);
                            imagenRuta = saveOriginalImage(imageUri);
                            etImage.setText("Imagen seleccionada ✅");
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar imagen: ", e);
                            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void configurarBotones() {
        btnCalendario.setOnClickListener(v -> mostrarCalendario());
        etFecha.setOnClickListener(v -> mostrarCalendario());
        btnSubir.setOnClickListener(v -> seleccionarImagen());

        // Configurar botón "Crear" con gradiente
        GradientDrawable gradientDrawableNormal = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#03683E"), Color.parseColor("#064349")});
        gradientDrawableNormal.setCornerRadius(80f);

        GradientDrawable gradientDrawablePressed = new GradientDrawable();
        gradientDrawablePressed.setColor(Color.parseColor("#063449"));
        gradientDrawablePressed.setCornerRadius(80f);

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, gradientDrawablePressed);
        stateListDrawable.addState(new int[]{}, gradientDrawableNormal);
        btnCrear.setBackground(stateListDrawable);

        btnCrear.setOnClickListener(v -> guardarActividad());
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private Bitmap procesarImagen(Uri imageUri) throws Exception {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
            BitmapFactory.decodeStream(is, null, options);
        }

        int targetWidth = dpToPx(TARGET_WIDTH_DP);
        int targetHeight = dpToPx(TARGET_HEIGHT_DP);
        options.inSampleSize = calcularFactorEscalado(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;

        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
            Bitmap bitmapOriginal = BitmapFactory.decodeStream(is, null, options);
            if (bitmapOriginal == null) return null;

            Bitmap scaledBitmap = escalarAlCentro(bitmapOriginal, targetWidth, targetHeight);
            return aplicarMascaraRedondeada(scaledBitmap, targetWidth, targetHeight);
        }
    }

    private int calcularFactorEscalado(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        while ((width / inSampleSize) > reqWidth * 2 || (height / inSampleSize) > reqHeight * 2) {
            inSampleSize *= 2;
        }

        return inSampleSize;
    }

    private Bitmap escalarAlCentro(Bitmap original, int targetWidth, int targetHeight) {
        float srcAspect = (float) original.getWidth() / original.getHeight();
        float dstAspect = (float) targetWidth / targetHeight;

        Rect srcRect = new Rect();
        if (srcAspect > dstAspect) {
            int srcWidth = (int) (original.getHeight() * dstAspect);
            int left = (original.getWidth() - srcWidth) / 2;
            srcRect.set(left, 0, left + srcWidth, original.getHeight());
        } else {
            int srcHeight = (int) (original.getWidth() / dstAspect);
            int top = (original.getHeight() - srcHeight) / 2;
            srcRect.set(0, top, original.getWidth(), top + srcHeight);
        }

        return Bitmap.createBitmap(original, srcRect.left, srcRect.top, srcRect.width(), srcRect.height());
    }

    private Bitmap aplicarMascaraRedondeada(Bitmap bitmap, int ancho, int alto) {
        Bitmap output = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, ancho, alto);
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawRoundRect(rectF, dpToPx(IMAGE_RADIUS_DP), dpToPx(IMAGE_RADIUS_DP), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);
        return output;
    }

    private String saveOriginalImage(Uri imageUri) throws Exception {
        File directory = new File(getFilesDir(), "actividad_imagenes");
        if (!directory.exists()) directory.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        File file = new File(directory, "IMG_" + timeStamp + ".jpg");

        try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return file.getAbsolutePath();
    }

    private void mostrarCalendario() {
        DatePickerDialog dialogo = new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog,
                (view, año, mes, dia) -> actualizarFechaSeleccionada(año, mes, dia),
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH));
        dialogo.setTitle("Seleccione una fecha");
        dialogo.show();
    }

    private void actualizarFechaSeleccionada(int año, int mes, int dia) {
        calendario.set(Calendar.YEAR, año);
        calendario.set(Calendar.MONTH, mes);
        calendario.set(Calendar.DAY_OF_MONTH, dia);
        etFecha.setText(formatoFecha.format(calendario.getTime()));
    }

    private void guardarActividad() {
        if (!validarCamposObligatorios()) return;

        try {
            Date fechaSeleccionada = formatoFecha.parse(etFecha.getText().toString().trim());
            Date fechaActual = new Date();

            if (fechaSeleccionada.before(fechaActual)) {
                Toast.makeText(this, "La fecha no puede ser anterior a hoy", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Actividad actividad = new Actividad();
        actividad.setTitulo(etTitulo.getText().toString().trim());
        actividad.setDescripcion(etDescripcion.getText().toString().trim());
        actividad.setFecha(etFecha.getText().toString().trim());
        actividad.setLugar(etLugar.getText().toString().trim());
        actividad.setResponsables(etResponsables.getText().toString().trim());
        actividad.setIdCreador(sharedPreferences.getInt("user_id", -1));
        actividad.setEstado("activo");
        actividad.setImagenRuta(imagenRuta != null ? imagenRuta : "");

        long resultado = managerDb.insertarActividad(actividad);
        if (resultado != -1) {
            Toast.makeText(this, "Actividad creada exitosamente", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error al crear la actividad", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validarCamposObligatorios() {
        if (etTitulo.getText().toString().trim().isEmpty() ||
                etDescripcion.getText().toString().trim().isEmpty() ||
                etFecha.getText().toString().trim().isEmpty() ||
                etLugar.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}