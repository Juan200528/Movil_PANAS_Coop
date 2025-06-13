package com.juan.movil_panas_coop.model;

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

    private List<Actividad> actividadList;
    private OnActividadClickListener clickListener;
    private OnDetallesClickListener detallesListener;
    private OnAsistirClickListener asistirListener;
    private ManagerDb managerDb;
    private int userId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface OnActividadClickListener {
        void onActividadClick(Actividad actividad);
    }

    public interface OnDetallesClickListener {
        void onDetallesClick(Actividad actividad);
    }

    public interface OnAsistirClickListener {
        void onAsistirClick(Actividad actividad, int position);
    }

    public BuscarAdapter(List<Actividad> actividadList, OnActividadClickListener clickListener,
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

    private boolean esActividadPasada(Actividad actividad) {
        try {
            if (actividad.getFecha() == null || actividad.getFecha().isEmpty()) {
                Log.w("BuscarAdapter", "Fecha nula o vacía para actividad: " + actividad.getTitulo());
                return false;
            }
            Date fechaActividad = dateFormat.parse(actividad.getFecha());
            Date hoy = new Date();
            boolean esPasada = fechaActividad != null && fechaActividad.before(hoy);
            Log.d("BuscarAdapter", "Verificando si es pasada - Título: " + actividad.getTitulo() +
                    ", Fecha: " + actividad.getFecha() + ", Hoy: " + dateFormat.format(hoy) +
                    ", Es pasada: " + esPasada);
            return esPasada;
        } catch (ParseException e) {
            Log.e("BuscarAdapter", "Error parsing date: " + actividad.getFecha(), e);
            return false;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Actividad actividad = actividadList.get(position);
        if (esActividadPasada(actividad)) {
            Log.d("BuscarAdapter", "Asignando VIEW_TYPE_PASADA a: " + actividad.getTitulo() + " con fecha: " + actividad.getFecha());
            return VIEW_TYPE_PASADA;
        } else if (actividad.isPromocionada()) {
            Log.d("BuscarAdapter", "Asignando VIEW_TYPE_PROMOCIONADA a: " + actividad.getTitulo());
            return VIEW_TYPE_PROMOCIONADA;
        } else if (actividad.isAsistido()) {
            Log.d("BuscarAdapter", "Asignando VIEW_TYPE_ASISTIR a: " + actividad.getTitulo());
            return VIEW_TYPE_ASISTIR;
        } else {
            Log.d("BuscarAdapter", "Asignando VIEW_TYPE_ACTIVIDAD a: " + actividad.getTitulo());
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
        Actividad actividad = actividadList.get(position);
        Log.d("BuscarAdapter", "onBindViewHolder position: " + position + ", titulo: " + actividad.getTitulo() +
                ", isAsistido: " + actividad.isAsistido() + ", Promocionada: " + actividad.isPromocionada() +
                ", Pasada: " + esActividadPasada(actividad));

        if (holder instanceof PromocionadaViewHolder) {
            PromocionadaViewHolder promoHolder = (PromocionadaViewHolder) holder;
            promoHolder.tvTituloActividadPromocionada.setText(actividad.getTitulo() != null ? actividad.getTitulo() : "");

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
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, actividad.getTitulo());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta actividad promocionada: " + actividad.getTitulo() + "\n" + actividad.getDescripcion());
                    holder.itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Compartir actividad"));
                });
            }
        } else if (holder instanceof ActividadViewHolder) {
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

            if (actividadHolder.btnCompartir != null) {
                actividadHolder.btnCompartir.setOnClickListener(v -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, actividad.getTitulo());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta actividad: " + actividad.getTitulo() + "\n" + actividad.getDescripcion());
                    holder.itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Compartir actividad"));
                });
            }

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

            if (asistirHolder.btnCompartir != null) {
                asistirHolder.btnCompartir.setOnClickListener(v -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, actividad.getTitulo());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta actividad a la que asistiré: " + actividad.getTitulo() + "\n" + actividad.getDescripcion());
                    holder.itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Compartir actividad"));
                });
            }

            asistirHolder.itemView.setOnClickListener(v -> clickListener.onActividadClick(actividad));
        } else if (holder instanceof PasadaViewHolder) {
            PasadaViewHolder pasadaHolder = (PasadaViewHolder) holder;
            pasadaHolder.tvTituloActividadPasada.setText(actividad.getTitulo() != null ? actividad.getTitulo() : "");

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

            pasadaHolder.itemView.setOnClickListener(v -> clickListener.onActividadClick(actividad));
        }
    }

    private void mostrarDialogoDetalles(Actividad actividad, View itemView) {
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

        tvDetalleTitulo.setText(actividad.getTitulo());
        tvDetalleDescripcion.setText(actividad.getDescripcion());
        tvDetalleFecha.setText(actividad.getFecha());
        tvDetalleLugar.setText(actividad.getLugar());
        tvDetalleResponsables.setText(actividad.getResponsables());

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

        public PromocionadaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividadPromocionada = itemView.findViewById(R.id.tvTituloActividadPromocionada);
            ivActividadImagenPromocionada = itemView.findViewById(R.id.ivActividadImagenPromocionada);
            btnVerDetallesPromocionada = itemView.findViewById(R.id.btnVerDetallesPromocionada);
            btnAsistirPromocionada = itemView.findViewById(R.id.btnAsistirPromocionada);
        }
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

        public AsistirViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividad = itemView.findViewById(R.id.tvTituloActividadAsistir);
            ivActividadImagen = itemView.findViewById(R.id.ivActividadImagenAsistir);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnCancelarAsistencia = itemView.findViewById(R.id.btnCancelarAsistencia);
            btnCompartir = itemView.findViewById(R.id.btnCompartir);
        }
    }

    static class PasadaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividadPasada;
        ImageView ivActividadImagenPasada;
        Button btnVerDetallesPasada;

        public PasadaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividadPasada = itemView.findViewById(R.id.tvTituloActividadUsuario);
            ivActividadImagenPasada = itemView.findViewById(R.id.ivActividadImagenUsuario);
            btnVerDetallesPasada = itemView.findViewById(R.id.btnVerDetallesUsuario);
        }
    }
}