package com.juan.movil_panas_coop.models;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.db.ManagerDb;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ActividadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class Item {
        public static final int TYPE_ACTIVIDAD = 1;
        public static final int TYPE_TITULO = 2;
        public static final int TYPE_PASADAS = 3;

        private final int type;
        private final Actividad actividad;
        private final String titulo;
        private final List<Actividad> actividadesPasadas;

        public Item(int type, Actividad actividad, String titulo, List<Actividad> actividadesPasadas) {
            this.type = type;
            this.actividad = actividad;
            this.titulo = titulo;
            this.actividadesPasadas = actividadesPasadas;
        }

        public int getType() {
            return type;
        }

        public Actividad getActividad() {
            return actividad;
        }

        public String getTitulo() {
            return titulo;
        }

        public List<Actividad> getActividadesPasadas() {
            return actividadesPasadas;
        }
    }

    private List<Item> itemList;
    private OnActividadClickListener clickListener;
    private OnEliminarClickListener eliminarListener;
    private OnEditarClickListener editarListener;
    private OnDetallesClickListener detallesListener;
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

    public interface OnDetallesClickListener {
        void onDetallesClick(Actividad actividad);
    }

    public ActividadAdapter(List<Item> itemList, OnActividadClickListener clickListener,
                            OnEliminarClickListener eliminarListener, OnEditarClickListener editarListener,
                            OnDetallesClickListener detallesListener) {
        this.itemList = itemList;
        this.clickListener = clickListener;
        this.eliminarListener = eliminarListener;
        this.editarListener = editarListener;
        this.detallesListener = detallesListener;
        this.managerDb = new ManagerDb(null); // Inicialización temporal, se sobrescribirá con setManagerDb
    }

    public void setManagerDb(ManagerDb managerDb) {
        this.managerDb = managerDb;
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Item.TYPE_ACTIVIDAD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad, parent, false);
            return new ActividadViewHolder(view);
        } else if (viewType == Item.TYPE_TITULO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad_titulo, parent, false);
            return new TituloViewHolder(view);
        } else { // Item.TYPE_PASADAS
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad_pasadas, parent, false);
            return new PasadasViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = itemList.get(position);

        if (item.getType() == Item.TYPE_ACTIVIDAD) {
            ActividadViewHolder actividadHolder = (ActividadViewHolder) holder;
            Actividad actividad = item.getActividad();

            actividadHolder.tvTituloActividad.setText(actividad.getTitulo() != null ? actividad.getTitulo() : "Sin título");
            actividadHolder.switchPromocion.setChecked(actividad.isPromocionada());
            actividadHolder.switchPromocion.setEnabled(!actividad.isPasada());

            actividadHolder.switchPromocion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!actividad.isPasada()) {
                    actividad.setPromocionada(isChecked);
                    if (managerDb != null) {
                        managerDb.open();
                        managerDb.actualizarActividad(actividad);
                        managerDb.close();
                    }
                }
            });

            String imagenRuta = actividad.getImagenRuta();
            if (imagenRuta != null && !imagenRuta.isEmpty()) {
                File imgFile = new File(imagenRuta);
                if (imgFile.exists()) {
                    actividadHolder.ivActividadImagen.setImageURI(Uri.fromFile(imgFile));
                } else {
                    actividadHolder.ivActividadImagen.setImageResource(R.drawable.default_image);
                }
            } else {
                actividadHolder.ivActividadImagen.setImageResource(R.drawable.default_image);
            }

            actividadHolder.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onActividadClick(actividad);
            });
            actividadHolder.btnVerDetalles.setOnClickListener(v -> {
                if (detallesListener != null) detallesListener.onDetallesClick(actividad);
            });

            if (actividad.isPasada()) {
                actividadHolder.btnEditar.setVisibility(View.VISIBLE);
                actividadHolder.btnEliminar.setVisibility(View.VISIBLE);
                actividadHolder.btnEditar.setOnClickListener(v -> {
                    if (editarListener != null) editarListener.onEditarClick(actividad);
                });
                actividadHolder.btnEliminar.setOnClickListener(v -> {
                    if (eliminarListener != null) eliminarListener.onEliminarClick(actividad);
                });

                // Desactivar funcionalidad para actividades pasadas
                actividadHolder.tvAgregarAsistentes.setEnabled(false);
                actividadHolder.btnPlus.setEnabled(false);
                actividadHolder.btnCompartir.setEnabled(false);
                actividadHolder.switchPromocion.setEnabled(false);
            } else {
                actividadHolder.btnEditar.setVisibility(View.VISIBLE);
                actividadHolder.btnEliminar.setVisibility(View.VISIBLE);
                actividadHolder.btnEditar.setOnClickListener(v -> {
                    if (editarListener != null) editarListener.onEditarClick(actividad);
                });
                actividadHolder.btnEliminar.setOnClickListener(v -> {
                    if (eliminarListener != null) eliminarListener.onEliminarClick(actividad);
                });

                actividadHolder.tvAgregarAsistentes.setEnabled(true);
                actividadHolder.btnPlus.setEnabled(true);
                actividadHolder.btnCompartir.setEnabled(true);

                View.OnClickListener navigateToGestionar = v -> {
                    // Aquí puedes navegar a un fragmento o actividad para gestionar asistentes
                    // Por ahora, solo mostramos un Toast
                    Toast.makeText(holder.itemView.getContext(), "Gestionar asistentes", Toast.LENGTH_SHORT).show();
                };

                actividadHolder.tvAgregarAsistentes.setOnClickListener(navigateToGestionar);
                actividadHolder.btnPlus.setOnClickListener(navigateToGestionar);
                actividadHolder.layoutAsistentes.setOnClickListener(navigateToGestionar);

                actividadHolder.btnCompartir.setOnClickListener(v -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, actividad.getTitulo() != null ? actividad.getTitulo() : "Actividad");
                    shareIntent.putExtra(Intent.EXTRA_TEXT,
                            "¡Mira esta actividad: " +
                                    (actividad.getTitulo() != null ? actividad.getTitulo() : "Sin título") + "\n" +
                                    (actividad.getDescripcion() != null ? actividad.getDescripcion() : ""));
                    holder.itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Compartir actividad"));
                });
            }
        } else if (item.getType() == Item.TYPE_TITULO) {
            TituloViewHolder tituloHolder = (TituloViewHolder) holder;
            tituloHolder.tvTituloSeccion.setText(item.getTitulo());
        } else { // Item.TYPE_PASADAS
            PasadasViewHolder pasadasHolder = (PasadasViewHolder) holder;
            List<Actividad> actividadesPasadas = item.getActividadesPasadas();

            ActividadAdapter pasadasAdapter = new ActividadAdapter(
                    actividadesPasadas.stream()
                            .map(a -> new Item(Item.TYPE_ACTIVIDAD, a, null, null))
                            .collect(Collectors.toList()),
                    clickListener, eliminarListener, editarListener, detallesListener);
            pasadasAdapter.setManagerDb(managerDb);
            pasadasHolder.recyclerActividadesPasadas.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            pasadasHolder.recyclerActividadesPasadas.setAdapter(pasadasAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividad;
        ImageView ivActividadImagen;
        Button btnVerDetalles;
        LinearLayout layoutAsistentes;
        TextView tvAgregarAsistentes;
        ImageButton btnPlus;
        ImageButton btnCompartir;
        ImageButton btnEditar;
        ImageButton btnEliminar;
        Switch switchPromocion;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividad = itemView.findViewById(R.id.tvTituloActividad);
            ivActividadImagen = itemView.findViewById(R.id.ivActividadImagen);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            layoutAsistentes = itemView.findViewById(R.id.layoutAsistentes);
            tvAgregarAsistentes = itemView.findViewById(R.id.tvAgregarAsistentes);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnCompartir = itemView.findViewById(R.id.btnCompartir);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            switchPromocion = itemView.findViewById(R.id.switchPromocion);
        }
    }

    static class TituloViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloSeccion;

        public TituloViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloSeccion = itemView.findViewById(R.id.tvTituloSeccion);
        }
    }

    static class PasadasViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerActividadesPasadas;

        public PasadasViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerActividadesPasadas = itemView.findViewById(R.id.recyclerActividadesPasadas);
        }
    }
}