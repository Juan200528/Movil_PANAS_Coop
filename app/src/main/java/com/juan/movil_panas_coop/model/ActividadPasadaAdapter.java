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
//public class ActividadPasadaAdapter extends RecyclerView.Adapter<ActividadPasadaAdapter.ActividadPasadaViewHolder> {
//
//    private List<Actividad> actividadList;
//    private OnDetallesClickListener detallesListener;
//
//    public interface OnDetallesClickListener {
//        void onDetallesClick(Actividad actividad);
//    }
//
//    public ActividadPasadaAdapter(List<Actividad> actividadList, OnDetallesClickListener detallesListener) {
//        this.actividadList = actividadList;
//        this.detallesListener = detallesListener;
//    }
//
//    @NonNull
//    @Override
//    public ActividadPasadaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad, parent, false);
//        return new ActividadPasadaViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ActividadPasadaViewHolder holder, int position) {
//        Actividad actividad = actividadList.get(position);
//
//        holder.tvTituloActividad.setText(actividad.getTitulo());
//
//        if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
//            File imgFile = new File(actividad.getImagenRuta());
//            if (imgFile.exists()) {
//                holder.ivActividadImagen.setImageURI(Uri.fromFile(imgFile));
//            } else {
//                holder.ivActividadImagen.setImageResource(R.drawable.default_image);
//            }
//        } else {
//            holder.ivActividadImagen.setImageResource(R.drawable.default_image);
//        }
//
//        holder.btnVerDetalles.setOnClickListener(v -> detallesListener.onDetallesClick(actividad));
//
//        holder.btnCompartir.setOnClickListener(v -> {
//            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//            shareIntent.setType("text/plain");
//            shareIntent.putExtra(Intent.EXTRA_SUBJECT, actividad.getTitulo());
//            shareIntent.putExtra(Intent.EXTRA_TEXT, "Recuerda esta actividad: " + actividad.getTitulo() + "\n" + actividad.getDescripcion());
//            holder.itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Compartir actividad"));
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return actividadList.size();
//    }
//
//    static class ActividadPasadaViewHolder extends RecyclerView.ViewHolder {
//        TextView tvTituloActividad;
//        ImageView ivActividadImagen;
//        Button btnVerDetalles;
//        ImageButton btnCompartir;
//
//        public ActividadPasadaViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvTituloActividad = itemView.findViewById(R.id.tvTituloActividad);
//            ivActividadImagen = itemView.findViewById(R.id.ivActividadImagen);
//            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
//            btnCompartir = itemView.findViewById(R.id.btnCompartir);
//        }
//    }
//}