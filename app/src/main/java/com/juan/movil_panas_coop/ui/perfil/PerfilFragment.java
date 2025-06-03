package com.juan.movil_panas_coop.ui.perfil;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.juan.movil_panas_coop.api.ApiService;
import com.juan.movil_panas_coop.api.RetrofitClient;
import com.juan.movil_panas_coop.db.ManagerDb;
import com.juan.movil_panas_coop.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private EditText editName, editEmail, editPhone, editAddress;
    private LinearLayout btnEditProfile, btnLogout;
    private Button btnSaveChanges;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        editPhone = view.findViewById(R.id.editPhone);
        editAddress = view.findViewById(R.id.editAddress);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        btnLogout = view.findViewById(R.id.btnLogout);

        sessionManager = new SessionManager(requireContext());

        cargarDatos();
        setCamposEditable(false);

        btnEditProfile.setOnClickListener(v -> {
            setCamposEditable(true);
            btnEditProfile.setVisibility(View.GONE);
            btnSaveChanges.setVisibility(View.VISIBLE);
        });

        btnSaveChanges.setOnClickListener(v -> {
            if (!validarCampos()) return;

            String userIdStr = sessionManager.getUserId();
            if (userIdStr != null && !userIdStr.isEmpty()) {
                int userId;
                try {
                    String cleaned = userIdStr.replaceAll("[^0-9]", "");
                    userId = Integer.parseInt(cleaned);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "ID inválido", Toast.LENGTH_SHORT).show();
                    return;
                }

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
                    cargarDatos();  // actualizar UI con nuevos datos
                    setCamposEditable(false);
                    btnSaveChanges.setVisibility(View.GONE);
                    btnEditProfile.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "ID de usuario no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> mostrarDialogoLogout());

        return view;
    }

    private void cargarDatos() {
        editName.setText(sessionManager.getUsername());
        editEmail.setText(sessionManager.getEmail());
        editPhone.setText(sessionManager.getPhone());
        editAddress.setText(sessionManager.getAddress());
    }

    private void guardarDatos() {
        sessionManager.guardarSesion(
                sessionManager.getUserId(),
                editName.getText().toString().trim(),
                editEmail.getText().toString().trim(),
                editPhone.getText().toString().trim()
        );
        sessionManager.guardarAddress(editAddress.getText().toString().trim());
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
        ApiService apiService = RetrofitClient.getApiService();
        apiService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) { }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { }
        });

        sessionManager.cerrarSesion();

        Intent intent = new Intent(requireActivity(), InicioSesion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
