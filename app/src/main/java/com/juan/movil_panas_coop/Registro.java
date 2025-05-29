package com.juan.movil_panas_coop;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.juan.movil_panas_coop.db.ManagerDb;

public class Registro extends AppCompatActivity {

    private EditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button btnRegistrar;
    private TextView loginLinkTextView;
    private ManagerDb managerDb;

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
        loginLinkTextView = findViewById(R.id.loginLinkTextView);

        // Asegurar que los campos de contraseña estén ocultos por defecto
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        confirmPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        // Limpiar cualquier fondo predeterminado
        btnRegistrar.setBackground(null);

        // Crear GradientDrawable para el estado normal (degradado)
        GradientDrawable gradientDrawableNormal = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] { Color.parseColor("#03683E"), Color.parseColor("#064349") });
        gradientDrawableNormal.setCornerRadius(80f);

        // Crear GradientDrawable para el estado presionado (color sólido)
        GradientDrawable gradientDrawablePressed = new GradientDrawable();
        gradientDrawablePressed.setColor(Color.parseColor("#063449"));
        gradientDrawablePressed.setCornerRadius(80f);

        // Configurar StateListDrawable para los estados del botón
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[] { android.R.attr.state_pressed }, gradientDrawablePressed);
        stateListDrawable.addState(new int[] {}, gradientDrawableNormal);

        // Aplicar el StateListDrawable al botón de Registrar
        btnRegistrar.setBackground(stateListDrawable);

        // Configurar texto con dos colores
        String fullText = "¿Ya tienes cuenta? Entrar";
        SpannableString spannableString = new SpannableString(fullText);

        // Color texto normal (#064349)
        spannableString.setSpan(
                new ForegroundColorSpan(Color.parseColor("#064349")),
                0, 18, // Desde el inicio hasta antes de "Entrar"
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Color para "Entrar" (#39B1E0)
        spannableString.setSpan(
                new ForegroundColorSpan(Color.parseColor("#39B1E0")),
                18, fullText.length(), // Desde "Entrar" hasta el final
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Hacer "Entrar" clickable
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Registro.this, InicioSesion.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(android.text.TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#39B1E0")); // Forzar color #39B1E0
                ds.setUnderlineText(false); // Quitar subrayado
            }
        };

        spannableString.setSpan(
                clickableSpan,
                18, fullText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Configurar TextView para evitar color de enlace predeterminado
        loginLinkTextView.setText(spannableString);
        loginLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());
        loginLinkTextView.setHighlightColor(Color.TRANSPARENT); // Evitar resaltado de enlace

        // Inicializar ManagerDb
        managerDb = new ManagerDb(this);
        managerDb.open();

        // Configurar íconos para mostrar/ocultar contraseña
        setupPasswordToggle(passwordEditText);
        setupPasswordToggle(confirmPasswordEditText);

        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void setupPasswordToggle(final EditText editText) {
        // Establecer ícono inicial (ocultar contraseña)
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0);
        editText.setCompoundDrawablePadding(10);

        editText.setOnTouchListener((v, event) -> {
            if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width() - editText.getCompoundDrawablePadding())) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Mostrar contraseña
                    editText.setTransformationMethod(null);
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_on, 0);
                    editText.setSelection(editText.getText().length());
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    // Ocultar contraseña
                    editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0);
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
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

        // Verificar si el email ya está registrado
        if (managerDb.existeEmail(email)) {
            emailEditText.setError("Este correo ya está registrado");
            emailEditText.requestFocus();
            return;
        }

        // Registrar usuario en la base de datos
        long id = managerDb.insertarUsuario(nombreCompleto, email, password);

        if (id != -1) {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();

            // Pasar el email a la actividad de inicio de sesión
            Intent intent = new Intent(Registro.this, InicioSesion.class);
            intent.putExtra("email_registrado", email);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        managerDb.close();
        super.onDestroy();
    }
}