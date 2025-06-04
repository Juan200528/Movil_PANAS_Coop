package com.juan.movil_panas_coop.ui.recordatorio;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.db.ManagerDb;
import com.juan.movil_panas_coop.model.Actividad;
import com.juan.movil_panas_coop.model.Notificacion;
import com.juan.movil_panas_coop.model.NotificacionAdapter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordatorioFragment extends Fragment {

    private RecordatorioViewModel viewModel;
    private EditText etTituloActividad, etDiasActividad;
    private Button btnGuardarConfig;
    private LinearLayout containerConfig;
    private RecyclerView recyclerView;
    private NotificacionAdapter notificacionAdapter;
    private List<Notificacion> notificacionList;
    private int userId;
    private int activityId = -1;
    private ManagerDb managerDb;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recordatorio, container, false);

        // Inicializar ManagerDb
        managerDb = new ManagerDb(getContext());

        // Obtener userId
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(getContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return root;
        }

        // Obtener activityId si se pasó
        if (getArguments() != null) {
            activityId = getArguments().getInt("activity_id", -1);
        }

        // Inicializar vistas
        etTituloActividad = root.findViewById(R.id.etTituloActividad);
        etDiasActividad = root.findViewById(R.id.etDiasActividad);
        btnGuardarConfig = root.findViewById(R.id.btnGuardarConfig);
        containerConfig = root.findViewById(R.id.containerConfig);

        // Configurar RecyclerView
        notificacionList = new ArrayList<>();
        notificacionAdapter = new NotificacionAdapter(notificacionList, managerDb);
        recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(notificacionAdapter);
        containerConfig.addView(recyclerView);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(RecordatorioViewModel.class);

        // Cargar título de la actividad si se pasó activityId
        if (activityId != -1) {
            managerDb.open();
            Actividad actividad = managerDb.obtenerActividades().stream()
                    .filter(a -> a.getId() == activityId)
                    .findFirst().orElse(null);
            if (actividad != null) {
                etTituloActividad.setText(actividad.getTitulo());
                etTituloActividad.setEnabled(false); // No editable
            }
            managerDb.close();
        }

        // Observar cambios en las notificaciones
        viewModel.getNotificaciones().observe(getViewLifecycleOwner(), notificaciones -> {
            notificacionList.clear();
            notificacionList.addAll(notificaciones);
            notificacionAdapter.notifyDataSetChanged();
        });

        // Cargar notificaciones iniciales
        viewModel.cargarNotificaciones(userId);

        // Configurar botón de guardar
        btnGuardarConfig.setOnClickListener(v -> guardarConfiguracion());

        return root;
    }

    private void guardarConfiguracion() {
        String titulo = etTituloActividad.getText().toString().trim();
        String diasStr = etDiasActividad.getText().toString().trim();

        if (titulo.isEmpty()) {
            etTituloActividad.setError("El título es obligatorio");
            return;
        }
        if (diasStr.isEmpty()) {
            etDiasActividad.setError("Los días son obligatorios");
            return;
        }

        int dias;
        try {
            dias = Integer.parseInt(diasStr);
            if (dias <= 0) {
                etDiasActividad.setError("Los días deben ser mayores a 0");
                return;
            }
        } catch (NumberFormatException e) {
            etDiasActividad.setError("Ingresa un número válido");
            return;
        }

        managerDb.open();
        Actividad actividad = managerDb.obtenerActividades().stream()
                .filter(a -> a.getTitulo().equals(titulo) && (activityId == -1 || a.getId() == activityId))
                .findFirst().orElse(null);

        if (actividad == null) {
            Toast.makeText(getContext(), "Actividad no encontrada", Toast.LENGTH_SHORT).show();
            managerDb.close();
            return;
        }

        // Validar días máximos
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date fechaActividad = sdf.parse(actividad.getFecha());
            Date today = new Date();
            long diffInMillies = fechaActividad.getTime() - today.getTime();
            long diasMaximos = diffInMillies / (1000 * 60 * 60 * 24);
            if (dias > diasMaximos) {
                etDiasActividad.setError("Los días no pueden exceder " + diasMaximos);
                managerDb.close();
                return;
            }

            Notificacion notificacion = new Notificacion();
            notificacion.setMensaje("La actividad " + titulo + " está a punto de terminar.");
            notificacion.setFecha(actividad.getFecha());
            notificacion.setIdUsuario(userId);
            notificacion.setDias(dias);
            notificacion.setDiasRestantes(dias);
            notificacion.setNombreActividad(titulo);
            notificacion.setDiasActividad((int) diasMaximos);
            notificacion.setIdActividad(actividad.getId());

            // Verificar si ya existe una notificación para este usuario y actividad
            if (managerDb.existeNotificacion(userId, actividad.getId())) {
                // Actualizar la notificación existente
                int idNotificacion = managerDb.obtenerIdNotificacion(userId, actividad.getId());
                if (idNotificacion != -1) {
                    notificacion.setId(idNotificacion);
                    managerDb.actualizarNotificacion(notificacion);
                    Toast.makeText(getContext(), "Notificación actualizada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al actualizar notificación", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Insertar una nueva notificación
                viewModel.insertarNotificacion(notificacion);
                Toast.makeText(getContext(), "Notificación guardada", Toast.LENGTH_SHORT).show();
            }

            etDiasActividad.setText("");
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Error en la fecha de la actividad", Toast.LENGTH_SHORT).show();
        } finally {
            managerDb.close();
        }
    }
}