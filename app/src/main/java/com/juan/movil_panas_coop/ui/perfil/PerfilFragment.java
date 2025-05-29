package com.juan.movil_panas_coop.ui.perfil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.juan.movil_panas_coop.InicioSesion;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.db.ManagerDb;

public class PerfilFragment extends Fragment {

    private EditText editName;
    private EditText editEmail, editPhone, editAddress;
    private LinearLayout btnEditProfile;
    private Button btnSaveChanges;
    private LinearLayout btnLogout;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        // Referencias a vistas
        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        editPhone = view.findViewById(R.id.editPhone);
        editAddress = view.findViewById(R.id.editAddress);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        btnLogout = view.findViewById(R.id.btnLogout);

        // SharedPreferences
        prefs = requireActivity().getSharedPreferences("user_prefs", requireActivity().MODE_PRIVATE);

        // Cargar datos iniciales
        cargarDatos();
        setCamposEditable(false); // Campos no editables al inicio

        // Acción: Editar perfil
        btnEditProfile.setOnClickListener(v -> {
            setCamposEditable(true);
            btnEditProfile.setVisibility(View.GONE);
            btnSaveChanges.setVisibility(View.VISIBLE);
        });

        // Acción: Guardar cambios
        btnSaveChanges.setOnClickListener(v -> {
            if (!validarCampos()) return;

            int userId = prefs.getInt("user_id", -1);
            if (userId != -1) {
                ManagerDb managerDb = new ManagerDb(requireContext());
                boolean actualizado = managerDb.actualizarUsuario(
                        userId,
                        editName.getText().toString().trim(),
                        editEmail.getText().toString().trim(),
                        editPhone.getText().toString().trim(),
                        editAddress.getText().toString().trim()
                );

                if (actualizado) {
                    guardarDatos();
                    setCamposEditable(false);
                    btnSaveChanges.setVisibility(View.GONE);
                    btnEditProfile.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "ID de usuario no válido", Toast.LENGTH_SHORT).show();
            }
        });

        // Acción: Cerrar sesión con confirmación
        btnLogout.setOnClickListener(v -> mostrarDialogoLogout());

        return view;
    }

    private void cargarDatos() {
        editName.setText(prefs.getString("user_name", "Jane Cooper"));
        editEmail.setText(prefs.getString("user_email", "jane@example.com"));
        editPhone.setText(prefs.getString("user_phone", "N/A"));
        editAddress.setText(prefs.getString("user_address", "N/A"));
    }

    private void guardarDatos() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_name", editName.getText().toString().trim());
        editor.putString("user_email", editEmail.getText().toString().trim());
        editor.putString("user_phone", editPhone.getText().toString().trim());
        editor.putString("user_address", editAddress.getText().toString().trim());
        editor.apply();
    }

    private void setCamposEditable(boolean editable) {
        editName.setEnabled(editable);
        editEmail.setEnabled(editable);
        editPhone.setEnabled(editable);
        editAddress.setEnabled(editable);
    }

    private boolean validarCampos() {
        if (editName.getText().toString().trim().isEmpty() ||
                editEmail.getText().toString().trim().isEmpty() ||
                editPhone.getText().toString().trim().isEmpty() ||
                editAddress.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Todos los campos deben estar completos", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void mostrarDialogoLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> cerrarSesion())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cerrarSesion() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(requireActivity(), InicioSesion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
