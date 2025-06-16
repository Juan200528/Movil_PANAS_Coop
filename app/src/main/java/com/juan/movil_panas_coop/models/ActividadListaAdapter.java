package com.juan.movil_panas_coop.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.model.ActividadModel;
import com.juan.movil_panas_coop.ui.promocionada.PromocionadaViewModel;

import java.util.List;

public class ActividadListaAdapter extends RecyclerView.Adapter<ActividadListaAdapter.ViewHolder> {

    private List<ActividadModel> actividades;
    private PromocionadaViewModel viewModel;

    public ActividadListaAdapter(List<ActividadModel> actividades, PromocionadaViewModel viewModel) {
        this.actividades = actividades;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista_actividades, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActividadModel actividad = actividades.get(position);
        holder.bind(actividad);
    }

    @Override
    public int getItemCount() {
        return actividades != null ? actividades.size() : 0;
    }

    public void setActividades(List<ActividadModel> actividades) {
        this.actividades = actividades;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitulo;
        private final ImageView ivImagen;
        private final Button btnVerDetalles;
        private final Button btnAsistir;
        private final Switch switchPromocionar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividadLista);
            ivImagen = itemView.findViewById(R.id.ivActividadImagenLista);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnAsistir = itemView.findViewById(R.id.btnAsistirActividad);
            switchPromocionar = itemView.findViewById(R.id.switchPromocionar);
        }

        public void bind(ActividadModel actividad) {
            tvTitulo.setText(actividad.getTitle());
            ivImagen.setImageResource(R.drawable.default_image); // Imagen por defecto

            btnVerDetalles.setOnClickListener(v -> {
                // Implementar lógica para ver detalles
                Toast.makeText(v.getContext(), "Ver detalles de: " + actividad.getTitle(), Toast.LENGTH_SHORT).show();
            });

            btnAsistir.setOnClickListener(v -> {
                // Implementar lógica para asistir
                Toast.makeText(v.getContext(), "Asistir a: " + actividad.getTitle(), Toast.LENGTH_SHORT).show();
            });



            // Configurar switch de promoción
            switchPromocionar.setChecked(actividad.isPromoted());
            switchPromocionar.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (viewModel != null) {
                    if (isChecked) {
                        viewModel.promocionarActividad(actividad.getId());
                    }
                    // La actividad se actualizará a través del ViewModel
                }
            });
        }
    }
}