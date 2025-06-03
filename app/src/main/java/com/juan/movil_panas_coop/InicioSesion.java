package com.juan.movil_panas_coop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
        if (sessionManager.isLoggedIn()) {
            redirigirAMenu();
            return;
        }

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        tvRegistrarse = findViewById(R.id.tvRegistro);

        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());

        // Navegar a la pantalla de registro
        tvRegistrarse.setOnClickListener(v -> {
            Intent intent = new Intent(InicioSesion.this, Registro.class); // Asegúrate de tener esta actividad
            startActivity(intent);
        });
    }

    private void iniciarSesion() {
        String email = etCorreo.getText().toString().trim();
        String password = etContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etCorreo.setError("Ingrese su correo electrónico");
            etCorreo.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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

                    Toast.makeText(InicioSesion.this, "ID de usuario: " + loginResponse.getId(), Toast.LENGTH_SHORT).show();

                    sessionManager.guardarSesion(
                            loginResponse.getId(),              // ID como String
                            loginResponse.getUsername(),
                            loginResponse.getEmail(),
                            "N/A"                               // Puedes cambiar esto si tienes teléfono
                    );
                    sessionManager.guardarToken(loginResponse.getToken());

                    redirigirAMenu();
                    Toast.makeText(InicioSesion.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Credenciales incorrectas";
                        Toast.makeText(InicioSesion.this, "Error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(InicioSesion.this, "Error en el inicio de sesión", Toast.LENGTH_SHORT).show();
                    }
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
