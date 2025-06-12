package com.juan.movil_panas_coop.models;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
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
import com.juan.movil_panas_coop.api.ApiService;
import com.juan.movil_panas_coop.api.RetrofitClient;
import com.juan.movil_panas_coop.db.ManagerDb;
import com.juan.movil_panas_coop.model.ActividadModel;
import com.juan.movil_panas_coop.model.PromotionRequest;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

            // Map Actividad to ActividadModel
            ActividadModel actividadModel = new ActividadModel();
            actividadModel.setId(String.valueOf(actividad.getId()));
            actividadModel.setTitle(actividad.getTitulo());
            actividadModel.setDescription(actividad.getDescripcion());
            actividadModel.setPromocionada(actividad.isPromocionada());
            actividadModel.setPasada(actividad.isPasada());

            actividadHolder.tvTituloActividad.setText(actividadModel.getTitle() != null ? actividadModel.getTitle() : "Sin título");
            actividadHolder.switchPromocion.setChecked(actividadModel.isPromocionada());
            actividadHolder.switchPromocion.setEnabled(!actividadModel.isPasada());

            actividadHolder.switchPromocion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!actividadModel.isPasada()) {
                    if (actividadModel.getId() == null || actividadModel.getId().equals("0")) {
                        Log.e("ActividadAdapter", "Error: ID de actividad no válido");
                        Toast.makeText(buttonView.getContext(), "Error: ID de actividad no válido", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    actividadModel.setPromocionada(isChecked);

                    String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    String endDate = ""; // Configure end date if needed

                    PromotionRequest request = new PromotionRequest(actividadModel.getId(), isChecked, startDate, endDate);

                    ApiService apiService = RetrofitClient.getApiService();
                    Call<ResponseBody> call = apiService.promoteTask(actividadModel.getId(), request);

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(buttonView.getContext(), "Promoción actualizada", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("ActividadAdapter", "Error al actualizar promoción: Código HTTP " + response.code());
                                Toast.makeText(buttonView.getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(buttonView.getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            // Handle image loading
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

            // Handle click listeners
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

                // Disable functionality for past activities
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

            if (actividadesPasadas != null && !actividadesPasadas.isEmpty()) {
                ActividadAdapter pasadasAdapter = new ActividadAdapter(
                        actividadesPasadas.stream()
                                .map(a -> new Item(Item.TYPE_ACTIVIDAD, a, null, null))
                                .collect(Collectors.toList()),
                        clickListener, eliminarListener, editarListener, detallesListener);
                pasadasAdapter.setManagerDb(managerDb);
                LinearLayoutManager layoutManager = new LinearLayoutManager(holder.itemView.getContext());
                pasadasHolder.recyclerActividadesPasadas.setLayoutManager(layoutManager);
                pasadasHolder.recyclerActividadesPasadas.setHasFixedSize(true);
                pasadasHolder.recyclerActividadesPasadas.setNestedScrollingEnabled(true);
                pasadasHolder.recyclerActividadesPasadas.setAdapter(pasadasAdapter);
            } else {
                pasadasHolder.recyclerActividadesPasadas.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ActividadViewHolder extends RecyclerView.ViewHolder {
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

    class TituloViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloSeccion;

        public TituloViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloSeccion = itemView.findViewById(R.id.tvTituloSeccion);
        }
    }

    class PasadasViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerActividadesPasadas;

        public PasadasViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerActividadesPasadas = itemView.findViewById(R.id.recyclerActividadesPasadas);
        }
    }
}