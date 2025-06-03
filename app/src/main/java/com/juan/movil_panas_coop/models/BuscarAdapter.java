package com.juan.movil_panas_coop.models;

import android.app.Dialog;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.db.ManagerDb;
import java.io.File;
import java.util.List;

public class BuscarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_PROMOCIONADA = 1;
    private static final int VIEW_TYPE_USUARIO = 2;

    private List<Actividad> actividadList;
    private OnActividadClickListener clickListener;
    private OnEliminarClickListener eliminarListener;
    private OnEditarClickListener editarListener;
    private ManagerDb managerDb;

    public interface OnActividadClickListener {
        void onActividadClick(Actividad actividad);
    }

    public interface OnEliminarClickListener {
        void onEliminarClick(Actividad actividad);
    }

    public interface OnEditarClickListener {
        void onEditarClick(Actividad actividad);
    }

    public BuscarAdapter(List<Actividad> actividadList, OnActividadClickListener clickListener,
                         OnEliminarClickListener eliminarListener, OnEditarClickListener editarListener) {
        this.actividadList = actividadList;
        this.clickListener = clickListener;
        this.eliminarListener = eliminarListener;
        this.editarListener = editarListener;
        this.managerDb = new ManagerDb(null);
    }

    public void setManagerDb(ManagerDb managerDb) {
        this.managerDb = managerDb;
    }

    @Override
    public int getItemViewType(int position) {
        Actividad actividad = actividadList.get(position);
        boolean isPromocionada = actividad.isPromocionada();
        Log.d("BuscarAdapter", "Posición: " + position + ", Título: " + actividad.getTitulo() + ", Promocionada: " + isPromocionada);
        return isPromocionada ? VIEW_TYPE_PROMOCIONADA : VIEW_TYPE_USUARIO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_PROMOCIONADA) {
            Log.d("BuscarAdapter", "Inflando item_actividad_promocionada.xml para VIEW_TYPE_PROMOCIONADA");
            View view = inflater.inflate(R.layout.item_actividad_promocionada, parent, false);
            return new PromocionadaViewHolder(view);
        } else {
            Log.d("BuscarAdapter", "Inflando item_actividad_usuario.xml para VIEW_TYPE_USUARIO");
            View view = inflater.inflate(R.layout.item_actividad_usuario, parent, false);
            return new ActividadViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Actividad actividad = actividadList.get(position);
        Log.d("BuscarAdapter", "Vinculando actividad: " + actividad.getTitulo() + ", Promocionada: " + actividad.isPromocionada());
        if (holder instanceof PromocionadaViewHolder) {
            PromocionadaViewHolder promoHolder = (PromocionadaViewHolder) holder;
            if (promoHolder.tvTituloActividadPromocionada == null) {
                Log.e("BuscarAdapter", "tvTituloActividadPromocionada es null en PromocionadaViewHolder");
                return;
            }
            promoHolder.tvTituloActividadPromocionada.setText(actividad.getTitulo());
            if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
                Log.d("BuscarAdapter", "Cargando imagen para actividad promocionada: " + actividad.getImagenRuta());
                File imgFile = new File(actividad.getImagenRuta());
                if (imgFile.exists()) {
                    promoHolder.ivActividadImagenPromocionada.setImageURI(Uri.fromFile(imgFile));
                } else {
                    Log.w("BuscarAdapter", "Imagen no encontrada, usando default_image");
                    promoHolder.ivActividadImagenPromocionada.setImageResource(R.drawable.default_image);
                }
            } else {
                Log.w("BuscarAdapter", "Ruta de imagen nula o vacía para actividad promocionada");
                promoHolder.ivActividadImagenPromocionada.setImageResource(R.drawable.default_image);
            }
            promoHolder.itemView.setOnClickListener(v -> clickListener.onActividadClick(actividad));
            promoHolder.btnVerDetallesPromocionada.setOnClickListener(v -> mostrarDialogoDetalles(actividad, promoHolder.itemView));
        } else if (holder instanceof ActividadViewHolder) {
            ActividadViewHolder userHolder = (ActividadViewHolder) holder;
            if (userHolder.tvTituloActividadUsuario == null) {
                Log.e("BuscarAdapter", "tvTituloActividadUsuario es null en ActividadViewHolder");
                return;
            }
            if (userHolder.ivActividadImagenUsuario == null) {
                Log.e("BuscarAdapter", "ivActividadImagenUsuario es null en ActividadViewHolder");
                return;
            }
            if (userHolder.btnVerDetallesUsuario == null) {
                Log.e("BuscarAdapter", "btnVerDetallesUsuario es null en ActividadViewHolder");
                return;
            }
            userHolder.tvTituloActividadUsuario.setText(actividad.getTitulo());
            if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
                Log.d("BuscarAdapter", "Cargando imagen para actividad no promocionada: " + actividad.getImagenRuta());
                File imgFile = new File(actividad.getImagenRuta());
                if (imgFile.exists()) {
                    userHolder.ivActividadImagenUsuario.setImageURI(Uri.fromFile(imgFile));
                } else {
                    Log.w("BuscarAdapter", "Imagen no encontrada, usando default_image");
                    userHolder.ivActividadImagenUsuario.setImageResource(R.drawable.default_image);
                }
            } else {
                Log.w("BuscarAdapter", "Ruta de imagen nula o vacía para actividad no promocionada");
                userHolder.ivActividadImagenUsuario.setImageResource(R.drawable.default_image);
            }
            userHolder.itemView.setOnClickListener(v -> clickListener.onActividadClick(actividad));
            userHolder.btnVerDetallesUsuario.setOnClickListener(v -> mostrarDialogoDetalles(actividad, userHolder.itemView));
        } else {
            Log.e("BuscarAdapter", "ViewHolder desconocido: " + holder.getClass().getSimpleName());
        }
    }

    private void mostrarDialogoDetalles(Actividad actividad, View parentView) {
        // Crear un Dialog
        Dialog dialog = new Dialog(parentView.getContext());
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Ajustar el tamaño del diálogo
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (parentView.getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        // Inicializar las vistas del diálogo
        ImageView ivImagenDetalle = dialog.findViewById(R.id.ivImagenDetalle);
        TextView tvDetalleTitulo = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDetalleDescripcion = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvDetalleFecha = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvDetalleLugar = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvDetalleResponsables = dialog.findViewById(R.id.tvResponsablesDetalle);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);

        // Llenar las vistas con los datos de la actividad
        tvDetalleTitulo.setText(actividad.getTitulo());
        tvDetalleDescripcion.setText(actividad.getDescripcion());
        tvDetalleFecha.setText(actividad.getFecha());
        tvDetalleLugar.setText(actividad.getLugar());
        tvDetalleResponsables.setText(actividad.getResponsables());

        // Cargar imagen desde archivo usando URI
        if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
            File imgFile = new File(actividad.getImagenRuta());
            if (imgFile.exists()) {
                ivImagenDetalle.setImageURI(Uri.fromFile(imgFile));
            }
        } else {
            ivImagenDetalle.setImageResource(R.drawable.default_image);
        }

        // Configurar el botón Volver
        btnVolver.setOnClickListener(v -> dialog.dismiss());

        Log.d("BuscarAdapter", "Mostrando diálogo de detalles para actividad: " + actividad.getTitulo());

        // Mostrar el diálogo
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return actividadList.size();
    }

    static class PromocionadaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividadPromocionada;
        ImageView ivActividadImagenPromocionada;
        Button btnVerDetallesPromocionada;

        public PromocionadaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividadPromocionada = itemView.findViewById(R.id.tvTituloActividadPromocionada);
            ivActividadImagenPromocionada = itemView.findViewById(R.id.ivActividadImagenPromocionada);
            btnVerDetallesPromocionada = itemView.findViewById(R.id.btnVerDetallesPromocionada);
        }
    }

    static class ActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividadUsuario;
        ImageView ivActividadImagenUsuario;
        Button btnVerDetallesUsuario;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividadUsuario = itemView.findViewById(R.id.tvTituloActividadUsuario);
            ivActividadImagenUsuario = itemView.findViewById(R.id.ivActividadImagenUsuario);
            btnVerDetallesUsuario = itemView.findViewById(R.id.btnVerDetallesUsuario);
        }
    }
}