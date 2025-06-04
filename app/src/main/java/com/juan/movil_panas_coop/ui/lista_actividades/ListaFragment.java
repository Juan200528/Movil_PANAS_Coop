package com.juan.movil_panas_coop.ui.lista_actividades;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.db.ManagerDb;
import com.juan.movil_panas_coop.models.Actividad;
import com.juan.movil_panas_coop.models.ActividadAdapterLista;
import com.juan.movil_panas_coop.models.Asistente;
import java.util.ArrayList;
import java.util.List;

public class ListaFragment extends Fragment implements ActividadAdapterLista.OnActividadClickListener,
        ActividadAdapterLista.OnDetallesClickListener, ActividadAdapterLista.OnAsistirClickListener {

    private RecyclerView recyclerView;
    private ActividadAdapterLista adapter;
    private List<Actividad> actividadList;
    private ManagerDb managerDb;
    private ListaViewModel viewModel;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista, container, false);

        recyclerView = view.findViewById(R.id.recyclerLista);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        actividadList = new ArrayList<>();
        managerDb = new ManagerDb(getContext());

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(getContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return view;
        }

        adapter = new ActividadAdapterLista(actividadList, this, this, this, userId);
        adapter.setManagerDb(managerDb);
        recyclerView.setAdapter(adapter);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ListaViewModel.class);
        viewModel.init(getContext(), userId);

        // Observar los datos del ViewModel
        viewModel.getActividades().observe(getViewLifecycleOwner(), actividades -> {
            actividadList.clear();
            actividadList.addAll(actividades);
            adapter.notifyDataSetChanged();
        });

        return view;
    }

    @Override
    public void onActividadClick(Actividad actividad) {
        // Este método puede ser usado para acciones al hacer clic en la actividad, si es necesario
    }

    @Override
    public void onDetallesClick(Actividad actividad) {
        // Este método ya no necesita lógica adicional, ya que el diálogo se maneja en el adaptador
    }

    @Override
    public void onAsistirClick(Actividad actividad, int position) {
        mostrarDialogoAsistir(actividad, position);
    }

    private void mostrarDialogoAsistir(Actividad actividad, int position) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_asistir);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etNombreCompleto = dialog.findViewById(R.id.etNombreAsistir);
        EditText etEmail = dialog.findViewById(R.id.etEmailAsistir);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);
        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);

        managerDb.open();
        String[] datosUsuario = managerDb.obtenerDatosUsuarioPorId(userId);
        if (datosUsuario != null) {
            etNombreCompleto.setText(datosUsuario[1] != null ? datosUsuario[1] : "");
            etEmail.setText(datosUsuario[2] != null ? datosUsuario[2] : "");
        }
        managerDb.close();

        btnConfirmar.setOnClickListener(v -> {
            String nombreCompleto = etNombreCompleto.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (!nombreCompleto.isEmpty() && !email.isEmpty()) {
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(getContext(), "Por favor, ingresa un correo válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                managerDb.open();
                Asistente asistente = new Asistente();
                asistente.setIdAsistente(userId);
                asistente.setIdActividad(actividad.getId());
                asistente.setNombreCompleto(nombreCompleto);
                asistente.setCorreo(email);
                asistente.setActividadNombre(actividad.getTitulo());
                long result = managerDb.insertarAsistente(asistente);
                if (result != -1) {
                    actividad.setAsistido(true);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(getContext(), "Asistencia confirmada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al registrar asistencia", Toast.LENGTH_SHORT).show();
                }
                managerDb.close();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        ivCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}