package com.juan.movil_panas_coop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.api.ApiService;
import com.juan.movil_panas_coop.model.User;
import com.juan.movil_panas_coop.model.LoginResponse;
import com.juan.movil_panas_coop.utils.SessionManager;
import com.juan.movil_panas_coop.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Registro extends AppCompatActivity {
    private EditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button btnRegistrar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Inicializar vistas
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        sessionManager = new SessionManager(this);

        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombreCompleto = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(nombreCompleto)) {
            fullNameEditText.setError("Ingrese su nombre completo");
            fullNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Ingrese su correo electrónico");
            emailEditText.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Ingrese un correo válido");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Ingrese una contraseña");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            passwordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Las contraseñas no coinciden");
            confirmPasswordEditText.requestFocus();
            return;
        }

        // Crear objeto User para enviar al servidor
        User user = new User();
        user.setUsername(nombreCompleto);
        user.setEmail(email);
        user.setPassword(password);

        // Llamada al API
        ApiService apiService = RetrofitClient.getApiService();
        Call<LoginResponse> call = apiService.register(user);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Guardar sesión
                    sessionManager.guardarSesion(
                            loginResponse.getId(),
                            loginResponse.getUsername(),
                            loginResponse.getEmail(),
                            loginResponse.getToken()
                    );

                    // Redirigir al menú principal
                    Intent intent = new Intent(Registro.this, MenuActivity.class);
                    startActivity(intent);
                    finish();

                    Toast.makeText(Registro.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Toast.makeText(Registro.this, "Error en el registro: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(Registro.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(Registro.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}