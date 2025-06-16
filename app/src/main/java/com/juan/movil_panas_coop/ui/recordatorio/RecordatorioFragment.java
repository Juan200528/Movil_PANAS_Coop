package com.juan.movil_panas_coop.ui.recordatorio;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.db.ManagerDb;
import com.juan.movil_panas_coop.model.Notificacion;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecordatorioFragment extends Fragment {

    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter adapter;
    private List<Notificacion> notificationList;
    private ManagerDb managerDb;
    private int activityId = -1;
    private String activityDate;
    private String activityTitle;
    private int userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recordatorio, container, false);

        // Initialize RecyclerView
        recyclerViewNotifications = view.findViewById(R.id.recycler_view_notifications);
        if (recyclerViewNotifications != null) {
            recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            Toast.makeText(getContext(), "Error: RecyclerView no encontrado", Toast.LENGTH_SHORT).show();
        }

        // Initialize database and data
        managerDb = new ManagerDb(getContext());
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        if (recyclerViewNotifications != null) {
            recyclerViewNotifications.setAdapter(adapter);
        }

        // Get arguments
        Bundle args = getArguments();
        if (savedInstanceState != null) {
            activityId = savedInstanceState.getInt("activity_id", -1);
            activityDate = savedInstanceState.getString("activity_date");
            activityTitle = savedInstanceState.getString("activity_title");
            userId = savedInstanceState.getInt("user_id", -1);
        } else if (args != null) {
            activityId = args.getInt("activity_id", -1);
            activityDate = args.getString("activity_date");
            activityTitle = args.getString("activity_title");
            userId = args.getInt("user_id", -1);
        }
        if (activityId != -1 && activityDate != null && userId != -1) {
            managerDb.open();
            notificationList.addAll(managerDb.obtenerNotificacionesPorUsuario(userId));
            notificationList.removeIf(n -> n.getIdActividad() != activityId);
            managerDb.close();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            if (activityTitle != null) {
                Toast.makeText(getContext(), "Actividad asistida: " + activityTitle, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Datos incompletos", Toast.LENGTH_SHORT).show();
        }

        // Check for a button to add notifications
        Button btnAddNotification = view.findViewById(R.id.btnConfig);
        if (btnAddNotification != null) {
            btnAddNotification.setOnClickListener(v -> showAddNotificationDialog());
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("activity_id", activityId);
        outState.putString("activity_date", activityDate);
        outState.putString("activity_title", activityTitle);
        outState.putInt("user_id", userId);
    }

    private void showAddNotificationDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_asistir);

        // IDs según dialogo_asistir.xml
        EditText etDaysBefore = dialog.findViewById(R.id.etDiasActividad);
        Button btnSave = dialog.findViewById(R.id.btnGuardarConfig);
        Button btnCancel = dialog.findViewById(R.id.btnCancelar);

        if (etDaysBefore != null && btnSave != null && btnCancel != null) {
            etDaysBefore.setHint("Días antes de la actividad");
            etDaysBefore.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

            btnSave.setOnClickListener(v -> {
                String daysBeforeStr = etDaysBefore.getText().toString().trim();
                if (daysBeforeStr.isEmpty()) {
                    Toast.makeText(getContext(), "Ingresa la cantidad de días", Toast.LENGTH_SHORT).show();
                    return;
                }

                int daysBefore;
                try {
                    daysBefore = Integer.parseInt(daysBeforeStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Ingresa un número válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar calendar = Calendar.getInstance();
                try {
                    calendar.setTime(sdf.parse(activityDate));
                    calendar.add(Calendar.DAY_OF_MONTH, -daysBefore);
                    String notificationDate = sdf.format(calendar.getTime());

                    managerDb.open();
                    if (managerDb.existeNotificacion(userId, activityId)) {
                        Toast.makeText(getContext(), "Ya existe una notificación para esta actividad", Toast.LENGTH_SHORT).show();
                        managerDb.close();
                        dialog.dismiss();
                        return;
                    }

                    Notificacion notificacion = new Notificacion();
                    notificacion.setMensaje("Recordatorio: " + activityTitle);
                    notificacion.setFecha(notificationDate);
                    notificacion.setIdUsuario(userId);
                    notificacion.setDias(daysBefore);
                    notificacion.setDiasRestantes(daysBefore);
                    notificacion.setNombreActividad(activityTitle);
                    notificacion.setDiasActividad(daysBefore);
                    notificacion.setIdActividad(activityId);

                    long result = managerDb.insertarNotificacion(notificacion);
                    if (result != -1) {
                        notificacion.setId((int) result);
                        notificationList.add(notificacion);
                        if (adapter != null) {
                            adapter.notifyItemInserted(notificationList.size() - 1);
                        }
                        Toast.makeText(getContext(), "Notificación añadida para: " + activityTitle, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error al añadir notificación", Toast.LENGTH_SHORT).show();
                    }
                    managerDb.close();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Fecha de actividad inválida", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });

            btnCancel.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } else {
            Toast.makeText(getContext(), "Error: Diálogo no configurado correctamente", Toast.LENGTH_SHORT).show();
        }
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private List<Notificacion> notifications;

        public NotificationAdapter(List<Notificacion> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_configuracion, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Notificacion notificacion = notifications.get(position);
            holder.tvTituloActividad.setText(notificacion.getNombreActividad());
            holder.tvFechaActividad.setText(notificacion.getFecha());
            holder.tvLugarActividad.setText(String.format(Locale.getDefault(), "%d días antes", notificacion.getDias()));
            holder.itemView.setOnLongClickListener(v -> {
                managerDb.open();
                managerDb.eliminarNotificacion(notificacion.getId());
                managerDb.close();
                notifications.remove(position);
                if (adapter != null) {
                    notifyItemRemoved(position);
                }
                Toast.makeText(getContext(), "Notificación eliminada", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTituloActividad, tvFechaActividad, tvLugarActividad;

            ViewHolder(View itemView) {
                super(itemView);
                tvTituloActividad = itemView.findViewById(R.id.tvTituloActividad);
                tvFechaActividad = itemView.findViewById(R.id.tvFechaActividad);
                tvLugarActividad = itemView.findViewById(R.id.tvLugarActividad);
            }
        }
    }
}