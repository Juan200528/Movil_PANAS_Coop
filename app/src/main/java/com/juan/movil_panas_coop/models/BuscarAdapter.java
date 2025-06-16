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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BuscarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ACTIVIDAD = 1;
    private static final int VIEW_TYPE_ASISTIR = 2;
    private static final int VIEW_TYPE_PROMOCIONADA = 3;
    private static final int VIEW_TYPE_PASADA = 4;

    private List<com.juan.movil_panas_coop.model.ActividadModel> actividadList;
    private OnActividadClickListener clickListener;
    private OnDetallesClickListener detallesListener;
    private OnAsistirClickListener asistirListener;
    private OnConfigClickListener configListener;
    private ManagerDb managerDb;
    private int userId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnActividadClickListener {
        void onActividadClick(com.juan.movil_panas_coop.model.ActividadModel actividad);
    }

    public interface OnDetallesClickListener {
        void onDetallesClick(com.juan.movil_panas_coop.model.ActividadModel actividad);
    }

    public interface OnAsistirClickListener {
        void onAsistirClick(com.juan.movil_panas_coop.model.ActividadModel actividad, int position);
    }

    public interface OnConfigClickListener {
        void onConfigClick(com.juan.movil_panas_coop.model.ActividadModel actividad);
    }

    public BuscarAdapter(List<com.juan.movil_panas_coop.model.ActividadModel> actividadList,
                         OnActividadClickListener clickListener,
                         OnDetallesClickListener detallesListener,
                         OnAsistirClickListener asistirListener,
                         OnConfigClickListener configListener,
                         int userId) {
        this.actividadList = actividadList;
        this.clickListener = clickListener;
        this.detallesListener = detallesListener;
        this.asistirListener = asistirListener;
        this.configListener = configListener;
        this.managerDb = new ManagerDb(null);
        this.userId = userId;
    }

    public void setManagerDb(ManagerDb managerDb) {
        this.managerDb = managerDb;
    }

    private boolean esActividadPasada(com.juan.movil_panas_coop.model.ActividadModel actividad) {
        try {
            if (actividad.getDate() == null || actividad.getDate().isEmpty()) {
                Log.w("BuscarAdapter", "Fecha nula o vacía para actividad: " + actividad.getTitle());
                return false;
            }
            Date fechaActividad = dateFormat.parse(actividad.getDate());
            Date hoy = new Date();
            boolean esPasada = fechaActividad != null && fechaActividad.before(hoy);
            Log.d("BuscarAdapter", "Verificando si es pasada - Título: " + actividad.getTitle() +
                    ", Fecha: " + actividad.getDate() + ", Hoy: " + dateFormat.format(hoy) +
                    ", Es pasada: " + esPasada);
            return esPasada;
        } catch (ParseException e) {
            Log.e("BuscarAdapter", "Error parsing date: " + actividad.getDate(), e);
            return false;
        }
    }

    @Override
    public int getItemViewType(int position) {
        com.juan.movil_panas_coop.model.ActividadModel actividad = actividadList.get(position);
        if (esActividadPasada(actividad)) {
            Log.d("BuscarAdapter", "Asignando VIEW_TYPE_PASADA a: " + actividad.getTitle() + " con fecha: " + actividad.getDate());
            return VIEW_TYPE_PASADA;
        } else if (actividad.isPromoted()) {
            Log.d("BuscarAdapter", "Asignando VIEW_TYPE_PROMOCIONADA a: " + actividad.getTitle());
            return VIEW_TYPE_PROMOCIONADA;
        } else if (actividad.isAsistido()) {
            Log.d("BuscarAdapter", "Asignando VIEW_TYPE_ASISTIR a: " + actividad.getTitle());
            return VIEW_TYPE_ASISTIR;
        } else {
            Log.d("BuscarAdapter", "Asignando VIEW_TYPE_ACTIVIDAD a: " + actividad.getTitle());
            return VIEW_TYPE_ACTIVIDAD;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_PROMOCIONADA) {
            View view = inflater.inflate(R.layout.item_actividad_promocionada, parent, false);
            return new PromocionadaViewHolder(view);
        } else if (viewType == VIEW_TYPE_ACTIVIDAD) {
            View view = inflater.inflate(R.layout.item_actividad_lista, parent, false);
            return new ActividadViewHolder(view);
        } else if (viewType == VIEW_TYPE_ASISTIR) {
            View view = inflater.inflate(R.layout.item_asistir, parent, false);
            return new AsistirViewHolder(view);
        } else if (viewType == VIEW_TYPE_PASADA) {
            View view = inflater.inflate(R.layout.item_actividad_usuario, parent, false);
            Log.d("BuscarAdapter", "Inflando item_actividad_usuario para VIEW_TYPE_PASADA");
            return new PasadaViewHolder(view);
        }
        throw new IllegalArgumentException("Tipo de vista no soportado: " + viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        com.juan.movil_panas_coop.model.ActividadModel actividad = actividadList.get(position);
        Log.d("BuscarAdapter", "onBindViewHolder position: " + position + ", titulo: " + actividad.getTitle() +
                ", isAsistido: " + actividad.isAsistido() + ", Promocionada: " + actividad.isPromoted() +
                ", Pasada: " + esActividadPasada(actividad));

        if (holder instanceof PromocionadaViewHolder) {
            PromocionadaViewHolder promoHolder = (PromocionadaViewHolder) holder;
            promoHolder.tvTituloActividadPromocionada.setText(actividad.getTitle() != null ? actividad.getTitle() : "");
            if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
                File imgFile = new File(actividad.getImagenRuta());
                if (imgFile.exists()) {
                    promoHolder.ivActividadImagenPromocionada.setImageURI(Uri.fromFile(imgFile));
                } else {
                    promoHolder.ivActividadImagenPromocionada.setImageResource(R.drawable.default_image);
                }
            } else {
                promoHolder.ivActividadImagenPromocionada.setImageResource(R.drawable.default_image);
            }
            promoHolder.itemView.setOnClickListener(v -> clickListener.onActividadClick(actividad));
            promoHolder.btnVerDetallesPromocionada.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, holder.itemView);
                detallesListener.onDetallesClick(actividad);
            });
            promoHolder.btnAsistirPromocionada.setOnClickListener(v -> asistirListener.onAsistirClick(actividad, position));
            if (promoHolder.btnCompartirPromocionada != null) {
                promoHolder.btnCompartirPromocionada.setOnClickListener(v -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, actividad.getTitle());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta actividad promocionada: " + actividad.getTitle() + "\n" + actividad.getDescription());
                    holder.itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Compartir actividad"));
                });
            }
            if (promoHolder.btnConfig != null) {
                promoHolder.btnConfig.setOnClickListener(v -> configListener.onConfigClick(actividad));
            }
        } else if (holder instanceof ActividadViewHolder) {
            ActividadViewHolder actividadHolder = (ActividadViewHolder) holder;
            actividadHolder.tvTituloActividad.setText(actividad.getTitle() != null ? actividad.getTitle() : "");
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
            if (actividadHolder.btnCompartir != null) {
                actividadHolder.btnCompartir.setOnClickListener(v -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, actividad.getTitle());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta actividad: " + actividad.getTitle() + "\n" + actividad.getDescription());
                    holder.itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Compartir actividad"));
                });
            }
            actividadHolder.btnVerDetalles.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, holder.itemView);
                detallesListener.onDetallesClick(actividad);
            });
            actividadHolder.btnAsistirActividad.setOnClickListener(v -> asistirListener.onAsistirClick(actividad, position));
            if (actividadHolder.btnConfig != null) {
                actividadHolder.btnConfig.setOnClickListener(v -> configListener.onConfigClick(actividad));
            }
        } else if (holder instanceof AsistirViewHolder) {
            AsistirViewHolder asistirHolder = (AsistirViewHolder) holder;
            asistirHolder.tvTituloActividad.setText(actividad.getTitle() != null ? actividad.getTitle() : "");
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
                                .filter(a -> a.getIdActividad() == Integer.parseInt(actividad.getId()))
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
            if (asistirHolder.btnCompartir != null) {
                asistirHolder.btnCompartir.setOnClickListener(v -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, actividad.getTitle());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta actividad a la que asistiré: " + actividad.getTitle() + "\n" + actividad.getDescription());
                    holder.itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Compartir actividad"));
                });
            }
            if (asistirHolder.btnConfig != null) {
                asistirHolder.btnConfig.setOnClickListener(v -> configListener.onConfigClick(actividad));
            }
            asistirHolder.itemView.setOnClickListener(v -> clickListener.onActividadClick(actividad));
        } else if (holder instanceof PasadaViewHolder) {
            PasadaViewHolder pasadaHolder = (PasadaViewHolder) holder;
            pasadaHolder.tvTituloActividadPasada.setText(actividad.getTitle() != null ? actividad.getTitle() : "");
            if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
                File imgFile = new File(actividad.getImagenRuta());
                if (imgFile.exists()) {
                    pasadaHolder.ivActividadImagenPasada.setImageURI(Uri.fromFile(imgFile));
                } else {
                    pasadaHolder.ivActividadImagenPasada.setImageResource(R.drawable.default_image);
                }
            } else {
                pasadaHolder.ivActividadImagenPasada.setImageResource(R.drawable.default_image);
            }
            pasadaHolder.btnVerDetallesPasada.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, holder.itemView);
                detallesListener.onDetallesClick(actividad);
            });
            if (pasadaHolder.btnConfig != null) {
                pasadaHolder.btnConfig.setOnClickListener(v -> configListener.onConfigClick(actividad));
            }
            pasadaHolder.itemView.setOnClickListener(v -> clickListener.onActividadClick(actividad));
        }
    }

    private void mostrarDialogoDetalles(com.juan.movil_panas_coop.model.ActividadModel actividad, View itemView) {
        Dialog dialog = new Dialog(itemView.getContext());
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (itemView.getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
        TextView tvDetalleTitulo = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDetalleDescripcion = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvDetalleFecha = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvDetalleLugar = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvDetalleResponsables = dialog.findViewById(R.id.tvResponsablesDetalle);
        ImageView ivImagenDetalle = dialog.findViewById(R.id.ivImagenDetalle);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);

        tvDetalleTitulo.setText(actividad.getTitle());
        tvDetalleDescripcion.setText(actividad.getDescription());
        tvDetalleFecha.setText(actividad.getDate());
        tvDetalleLugar.setText(actividad.getPlace());
        tvDetalleResponsables.setText(String.join(", ", actividad.getResponsible()));

        if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
            File imgFile = new File(actividad.getImagenRuta());
            if (imgFile.exists()) {
                ivImagenDetalle.setImageURI(Uri.fromFile(imgFile));
            } else {
                ivImagenDetalle.setImageResource(R.drawable.default_image);
            }
        } else {
            ivImagenDetalle.setImageResource(R.drawable.default_image);
        }

        btnVolver.setOnClickListener(v -> dialog.dismiss());
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
        Button btnAsistirPromocionada;
        ImageButton btnCompartirPromocionada;
        ImageButton btnConfig;

        public PromocionadaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividadPromocionada = itemView.findViewById(R.id.tvTituloActividadPromocionada);
            ivActividadImagenPromocionada = itemView.findViewById(R.id.ivActividadImagenPromocionada);
            btnVerDetallesPromocionada = itemView.findViewById(R.id.btnVerDetallesPromocionada);
            btnAsistirPromocionada = itemView.findViewById(R.id.btnAsistirPromocionada);

            btnConfig = itemView.findViewById(R.id.btnConfig);
        }
    }

    static class ActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividad;
        ImageView ivActividadImagen;
        ImageButton btnCompartir;
        Button btnVerDetalles;
        Button btnAsistirActividad;
        ImageButton btnConfig;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividad = itemView.findViewById(R.id.tvTituloActividadLista);
            ivActividadImagen = itemView.findViewById(R.id.ivActividadImagenLista);
            btnCompartir = itemView.findViewById(R.id.btnCompartir);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnAsistirActividad = itemView.findViewById(R.id.btnAsistirActividad);
            btnConfig = itemView.findViewById(R.id.btnConfig);
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

    static class PasadaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividadPasada;
        ImageView ivActividadImagenPasada;
        Button btnVerDetallesPasada;
        ImageButton btnConfig;

        public PasadaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividadPasada = itemView.findViewById(R.id.tvTituloActividadUsuario);
            ivActividadImagenPasada = itemView.findViewById(R.id.ivActividadImagenUsuario);
            btnVerDetallesPasada = itemView.findViewById(R.id.btnVerDetallesUsuario);
            btnConfig = itemView.findViewById(R.id.btnConfig);
        }
    }
}