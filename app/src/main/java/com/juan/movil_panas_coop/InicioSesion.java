package com.juan.movil_panas_coop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.juan.movil_panas_coop.api.ApiService;
import com.juan.movil_panas_coop.api.RetrofitClient;
import com.juan.movil_panas_coop.model.LoginResponse;
import com.juan.movil_panas_coop.model.User;
import com.juan.movil_panas_coop.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioSesion extends AppCompatActivity {

    private EditText etCorreo, etContrasena;
    private Button btnIniciarSesion;
    private TextView tvRegistrarse;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);

        sessionManager = new SessionManager(this);
        RetrofitClient.init(getApplicationContext());

        if (sessionManager.isLoggedIn()) {
            redirigirAMenu();
            return;
        }

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        tvRegistrarse = findViewById(R.id.tvRegistro);

        // Configurar campo de contraseña
        etContrasena.setTransformationMethod(PasswordTransformationMethod.getInstance());
        setupPasswordToggle(etContrasena); // <-- Añadimos el toggle de mostrar/ocultar

        // Aplicar efecto visual al botón
        setupLoginButtonWithStateEffect();

        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());

        tvRegistrarse.setOnClickListener(v -> {
            Intent intent = new Intent(InicioSesion.this, Registro.class);
            startActivity(intent);
        });

        String emailRegistrado = getIntent().getStringExtra("email_registrado");
        if (emailRegistrado != null) {
            etCorreo.setText(emailRegistrado);
        }
    }

    private void setupPasswordToggle(final EditText editText) {
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0);
        editText.setCompoundDrawablePadding(10);

        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - editText.getCompoundDrawablePadding())) {
                    if (editText.getTransformationMethod() == null) {
                        // Ocultar contraseña
                        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0);
                    } else {
                        // Mostrar contraseña
                        editText.setTransformationMethod(null);
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_on, 0);
                    }
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    private void setupLoginButtonWithStateEffect() {
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

        btnIniciarSesion.setBackground(stateListDrawable);
    }

    private void iniciarSesion() {
        String email = etCorreo.getText().toString().trim();
        String password = etContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etCorreo.setError("Ingrese su correo electrónico");
            etCorreo.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etCorreo.setError("Ingrese un correo válido");
            etCorreo.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etContrasena.setError("Ingrese una contraseña");
            etContrasena.requestFocus();
            return;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        ApiService apiService = RetrofitClient.getApiService();
        Call<LoginResponse> call = apiService.login(user);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    String tokenCookie = null;
                    for (int i = 0; i < response.headers().size(); i++) {
                        String name = response.headers().name(i);
                        String value = response.headers().value(i);
                        if (name.equalsIgnoreCase("set-cookie") && value.contains("token=")) {
                            int start = value.indexOf("token=") + 6;
                            int end = value.indexOf(';', start);
                            tokenCookie = (end > start) ? value.substring(start, end) : value.substring(start);
                            break;
                        }
                    }

                    if (tokenCookie == null || tokenCookie.isEmpty()) {
                        Toast.makeText(InicioSesion.this, "No se recibió token de autenticación", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sessionManager.guardarToken(tokenCookie);
                    sessionManager.guardarSesion(
                            loginResponse.getId(),
                            loginResponse.getUsername(),
                            loginResponse.getEmail(),
                            "N/A"
                    );

                    Toast.makeText(InicioSesion.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                    redirigirAMenu();

                } else {
                    Toast.makeText(InicioSesion.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(InicioSesion.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirigirAMenu() {
        Intent intent = new Intent(InicioSesion.this, MenuActivity.class);
        startActivity(intent);
        finish();
    }
}