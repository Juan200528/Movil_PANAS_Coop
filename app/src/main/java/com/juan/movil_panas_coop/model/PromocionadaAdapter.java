//package com.juan.movil_panas_coop.model;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.juan.movil_panas_coop.R;
//import java.io.File;
//import java.util.List;
//
//public class PromocionadaAdapter extends RecyclerView.Adapter<PromocionadaAdapter.PromocionadaViewHolder> {
//
//    private List<Actividad> actividadList;
//    private OnActividadClickListener clickListener;
//    private OnDetallesClickListener detallesListener;
//
//    public interface OnActividadClickListener {
//        void onActividadClick(Actividad actividad);
//    }
//
//    public interface OnDetallesClickListener {
//        void onDetallesClick(Actividad actividad);
//    }
//
//    public PromocionadaAdapter(List<Actividad> actividadList, OnActividadClickListener clickListener,
//                               OnDetallesClickListener detallesListener) {
//        this.actividadList = actividadList;
//        this.clickListener = clickListener;
//        this.detallesListener = detallesListener;
//    }
//
//    @NonNull
//    @Override
//    public PromocionadaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad_promocionada, parent, false);
//        return new PromocionadaViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull PromocionadaViewHolder holder, int position) {
//        Actividad actividad = actividadList.get(position);
//
//        // Vincular datos a las vistas
//        holder.tvTituloActividadPromocionada.setText(actividad.getTitulo());
//
//        // Cargar imagen
//        if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
//            File imgFile = new File(actividad.getImagenRuta());
//            if (imgFile.exists()) {
//                holder.ivActividadImagenPromocionada.setImageURI(Uri.fromFile(imgFile));
//            } else {
//                holder.ivActividadImagenPromocionada.setImageResource(R.drawable.default_image);
//            }
//        } else {
//            holder.ivActividadImagenPromocionada.setImageResource(R.drawable.default_image);
//        }
//
//        // Configurar clics
//        holder.itemView.setOnClickListener(v -> clickListener.onActividadClick(actividad));
//        holder.btnVerDetallesPromocionada.setOnClickListener(v -> detallesListener.onDetallesClick(actividad));
//    }
//
//    @Override
//    public int getItemCount() {
//        return actividadList.size();
//    }
//
//    static class PromocionadaViewHolder extends RecyclerView.ViewHolder {
//        TextView tvTituloActividadPromocionada;
//        ImageView ivActividadImagenPromocionada;
//        Button btnVerDetallesPromocionada;
//
//        public PromocionadaViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvTituloActividadPromocionada = itemView.findViewById(R.id.tvTituloActividadPromocionada);
//            ivActividadImagenPromocionada = itemView.findViewById(R.id.ivActividadImagenPromocionada);
//            btnVerDetallesPromocionada = itemView.findViewById(R.id.btnVerDetallesPromocionada);
//        }
//    }
//
//    static class ActividadViewHolder extends RecyclerView.ViewHolder {
//        TextView tvTituloActividadPromocionada;
//        ImageView ivActividadImagenPromocionada;
//        Button btnVerDetallesPromocionada;
//
//        public ActividadViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvTituloActividadPromocionada = itemView.findViewById(R.id.tvTituloActividadPromocionada);
//            ivActividadImagenPromocionada = itemView.findViewById(R.id.ivActividadImagenPromocionada);
//            btnVerDetallesPromocionada = itemView.findViewById(R.id.btnVerDetallesPromocionada);
//        }
//    }
//}