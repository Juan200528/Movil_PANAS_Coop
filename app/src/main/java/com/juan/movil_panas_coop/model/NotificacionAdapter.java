//package com.juan.movil_panas_coop.model;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.juan.movil_panas_coop.R;
//import com.juan.movil_panas_coop.db.ManagerDb;
//import java.util.List;
//
//public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.NotificacionViewHolder> {
//
//    private List<Notificacion> notificacionList;
//    private ManagerDb managerDb;
//
//    public NotificacionAdapter(List<Notificacion> notificacionList, ManagerDb managerDb) {
//        this.notificacionList = notificacionList;
//        this.managerDb = managerDb;
//    }
//
//    @NonNull
//    @Override
//    public NotificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_configuracion, parent, false);
//        return new NotificacionViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull NotificacionViewHolder holder, int position) {
//        Notificacion notificacion = notificacionList.get(position);
//        holder.tvTituloActividad.setText(notificacion.getNombreActividad() != null ? notificacion.getNombreActividad() : "Sin título");
//        holder.tvFechaActividad.setText(notificacion.getFecha() != null ? notificacion.getFecha() : "Sin fecha");
//
//        // Obtener el lugar de la actividad
//        managerDb.open();
//        Actividad actividad = managerDb.obtenerActividadPorId(notificacion.getIdActividad());
//        managerDb.close();
//        holder.tvLugarActividad.setText(actividad != null && actividad.getLugar() != null ? actividad.getLugar() : "Sin lugar");
//    }
//
//    @Override
//    public int getItemCount() {
//        return notificacionList.size();
//    }
//
//    static class NotificacionViewHolder extends RecyclerView.ViewHolder {
//        TextView tvTituloActividad, tvFechaActividad, tvLugarActividad;
//
//        public NotificacionViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvTituloActividad = itemView.findViewById(R.id.tvTituloActividad);
//            tvFechaActividad = itemView.findViewById(R.id.tvFechaActividad);
//            tvLugarActividad = itemView.findViewById(R.id.tvLugarActividad);
//        }
//    }
//}