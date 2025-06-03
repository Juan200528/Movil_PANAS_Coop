package com.juan.movil_panas_coop.models;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.db.ManagerDb;
import java.io.File;
import java.util.List;

public class ActividadAdapterLista extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ACTIVIDAD = 1;
    private static final int VIEW_TYPE_ASISTIR = 2;

    private List<Actividad> actividadList;
    private OnActividadClickListener clickListener;
    private OnDetallesClickListener detallesListener;
    private OnAsistirClickListener asistirListener;
    private ManagerDb managerDb;
    private int userId;

    public interface OnActividadClickListener {
        void onActividadClick(Actividad actividad);
    }

    public interface OnDetallesClickListener {
        void onDetallesClick(Actividad actividad);
    }

    public interface OnAsistirClickListener {
        void onAsistirClick(Actividad actividad, int position);
    }

    public ActividadAdapterLista(List<Actividad> actividadList, OnActividadClickListener clickListener,
                                 OnDetallesClickListener detallesListener, OnAsistirClickListener asistirListener, int userId) {
        this.actividadList = actividadList;
        this.clickListener = clickListener;
        this.detallesListener = detallesListener;
        this.asistirListener = asistirListener;
        this.managerDb = new ManagerDb(null);
        this.userId = userId;
    }

    public void setManagerDb(ManagerDb managerDb) {
        this.managerDb = managerDb;
    }

    @Override
    public int getItemViewType(int position) {
        Actividad actividad = actividadList.get(position);
        Log.d("ActividadAdapter", "getItemViewType position: " + position + ", isAsistido: " + actividad.isAsistido());
        return actividad.isAsistido() ? VIEW_TYPE_ASISTIR : VIEW_TYPE_ACTIVIDAD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_ACTIVIDAD) {
            View view = inflater.inflate(R.layout.item_actividad_lista, parent, false);
            return new ActividadViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_asistir, parent, false);
            return new AsistirViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Actividad actividad = actividadList.get(position);
        Log.d("ActividadAdapter", "onBindViewHolder position: " + position + ", titulo: " + actividad.getTitulo() +
                ", isAsistido: " + actividad.isAsistido());

        if (holder instanceof ActividadViewHolder) {
            ActividadViewHolder actividadHolder = (ActividadViewHolder) holder;
            actividadHolder.tvTituloActividad.setText(actividad.getTitulo() != null ? actividad.getTitulo() : "");

            if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
                File imgFile = new File(actividad.getImagenRuta());
                if (imgFile.exists()) {
                    actividadHolder.ivActividadImagen.setImageURI(Uri.fromFile(imgFile));
                } else {
                    actividadHolder.ivActividadImagen.setImageResource(R.drawable.default_image);
                }
            } else {
                actividadHolder.ivActividadImagen.setImageResource(R.drawable.default_image);
            }

            actividadHolder.itemView.setOnClickListener(v -> clickListener.onActividadClick(actividad));

            actividadHolder.btnCompartir.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, actividad.getTitulo());
                shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta actividad: " + actividad.getTitulo() + "\n" + actividad.getDescripcion());
                holder.itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Compartir actividad"));
            });

            actividadHolder.btnVerDetalles.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, holder.itemView);
                detallesListener.onDetallesClick(actividad);
            });

            actividadHolder.btnAsistirActividad.setOnClickListener(v -> asistirListener.onAsistirClick(actividad, position));
        } else if (holder instanceof AsistirViewHolder) {
            AsistirViewHolder asistirHolder = (AsistirViewHolder) holder;
            asistirHolder.tvTituloActividad.setText(actividad.getTitulo() != null ? actividad.getTitulo() : "");

            if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
                File imgFile = new File(actividad.getImagenRuta());
                if (imgFile.exists()) {
                    asistirHolder.ivActividadImagen.setImageURI(Uri.fromFile(imgFile));
                } else {
                    asistirHolder.ivActividadImagen.setImageResource(R.drawable.default_image);
                }
            } else {
                asistirHolder.ivActividadImagen.setImageResource(R.drawable.default_image);
            }

            asistirHolder.btnVerDetalles.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, holder.itemView);
                detallesListener.onDetallesClick(actividad);
            });

            asistirHolder.btnCancelarAsistencia.setOnClickListener(v -> {
                Dialog dialog = new Dialog(holder.itemView.getContext());
                dialog.setContentView(R.layout.dialogo_cancelar_asistencia);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);
                Button btnCancelar = dialog.findViewById(R.id.btnCancelar);
                Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);

                ivCerrar.setOnClickListener(view -> dialog.dismiss());
                btnCancelar.setOnClickListener(view -> dialog.dismiss());

                btnConfirmar.setOnClickListener(view -> {
                    if (managerDb != null) {
                        managerDb.open();
                        List<Asistente> asistentes = managerDb.obtenerAsistentesPorUsuario(userId);
                        Asistente asistente = asistentes.stream()
                                .filter(a -> a.getIdActividad() == actividad.getId())
                                .findFirst().orElse(null);
                        if (asistente != null) {
                            managerDb.eliminarAsistente(asistente.getId());
                            actividad.setAsistido(false);
                            notifyItemChanged(position);
                            Toast.makeText(holder.itemView.getContext(), "Asistencia cancelada", Toast.LENGTH_SHORT).show();
                        }
                        managerDb.close();
                    } else {
                        Toast.makeText(holder.itemView.getContext(), "Error: Base de datos no inicializada", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                });

                dialog.show();
            });

            asistirHolder.btnCompartir.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, actividad.getTitulo());
                shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta actividad a la que asistiré: " + actividad.getTitulo() + "\n" + actividad.getDescripcion());
                holder.itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Compartir actividad"));
            });

          /*  asistirHolder.btnConfig.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("activity_id", actividad.getId());
                Navigation.findNavController(holder.itemView).navigate(R.id.nav_notificaciones, bundle);
            });*/

            asistirHolder.itemView.setOnClickListener(v -> clickListener.onActividadClick(actividad));
        }
    }

    private void mostrarDialogoDetalles(Actividad actividad, View itemView) {
        Dialog dialog = new Dialog(itemView.getContext());
        dialog.setContentView(R.layout.dialogo_detalle_actividad);

        // Configurar fondo transparente
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Ajustar el tamaño del diálogo
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (itemView.getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        // Inicializar las vistas del diálogo
        TextView tvDetalleTitulo = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDetalleDescripcion = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvDetalleFecha = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvDetalleLugar = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvDetalleResponsables = dialog.findViewById(R.id.tvResponsablesDetalle);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);

        // Llenar las vistas con los datos de la actividad
        ImageView ivImagenDetalle = dialog.findViewById(R.id.ivImagenDetalle);
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

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return actividadList.size();
    }

    static class ActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividad;
        ImageView ivActividadImagen;
        ImageButton btnCompartir;
        Button btnVerDetalles;
        Button btnAsistirActividad;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividad = itemView.findViewById(R.id.tvTituloActividadLista);
            ivActividadImagen = itemView.findViewById(R.id.ivActividadImagenLista);
            btnCompartir = itemView.findViewById(R.id.btnCompartir);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnAsistirActividad = itemView.findViewById(R.id.btnAsistirActividad);
        }
    }

    static class AsistirViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividad;
        ImageView ivActividadImagen;
        Button btnVerDetalles;
        Button btnCancelarAsistencia;
        ImageButton btnCompartir;
        ImageButton btnConfig;

        public AsistirViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividad = itemView.findViewById(R.id.tvTituloActividadAsistir);
            ivActividadImagen = itemView.findViewById(R.id.ivActividadImagenAsistir);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnCancelarAsistencia = itemView.findViewById(R.id.btnCancelarAsistencia);
            btnCompartir = itemView.findViewById(R.id.btnCompartir);
            btnConfig = itemView.findViewById(R.id.btnConfig);
        }
    }
}
