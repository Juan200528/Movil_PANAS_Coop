package com.juan.movil_panas_coop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
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

import com.juan.movil_panas_coop.db.ManagerDb;

public class InicioSesion extends AppCompatActivity {

    private EditText etCorreo, etContrasena;
    private Button btnIniciarSesion;
    private TextView tvRegistro;
    private ManagerDb managerDb;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        tvRegistro = findViewById(R.id.tvRegistro);

        etContrasena.setTransformationMethod(PasswordTransformationMethod.getInstance());

        managerDb = new ManagerDb(this);
        managerDb.open();

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        configurarBotonIniciarSesion();
        configurarTextoRegistrate();

        setupPasswordToggle(etContrasena);

        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());

        String emailRegistrado = getIntent().getStringExtra("email_registrado");
        if (emailRegistrado != null) {
            etCorreo.setText(emailRegistrado);
        }
    }

    private void configurarBotonIniciarSesion() {
        btnIniciarSesion.setBackground(null);

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

    private void configurarTextoRegistrate() {
        String fullText = "¿No tienes una cuenta? Registrate";
        SpannableString spannableString = new SpannableString(fullText);

        spannableString.setSpan(
                new ForegroundColorSpan(Color.parseColor("#064349")),
                0, 22,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        spannableString.setSpan(
                new ForegroundColorSpan(Color.parseColor("#39B1E0")),
                22, fullText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(InicioSesion.this, Registro.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(android.text.TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#39B1E0"));
                ds.setUnderlineText(false);
            }
        };

        spannableString.setSpan(
                clickableSpan,
                22, fullText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        tvRegistro.setText(spannableString);
        tvRegistro.setMovementMethod(LinkMovementMethod.getInstance());
        tvRegistro.setHighlightColor(Color.TRANSPARENT);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(final EditText editText) {
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0);
        editText.setCompoundDrawablePadding(10);

        editText.setOnTouchListener((v, event) -> {
            if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width() - editText.getCompoundDrawablePadding())) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    editText.setTransformationMethod(null);
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_on, 0);
                    editText.setSelection(editText.getText().length());
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0);
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    private void iniciarSesion() {
        String email = etCorreo.getText().toString().trim();
        String password = etContrasena.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = managerDb.validarUsuario(email, password);
        if (userId != -1) {
            String nombreCompleto = managerDb.getUserNameById(userId);
            String phone = managerDb.getUserPhoneById(userId);
            String address = managerDb.getUserAddressById(userId);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("user_id", userId);
            editor.putString("user_email", email);
            editor.putString("user_name", (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) ? nombreCompleto : "Usuario");
            editor.putString("user_phone", (phone != null) ? phone : "N/A");
            editor.putString("user_address", (address != null) ? address : "N/A");
            editor.apply();

            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Correo electrónico o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (managerDb != null) {
            managerDb.close();
        }
    }
}
