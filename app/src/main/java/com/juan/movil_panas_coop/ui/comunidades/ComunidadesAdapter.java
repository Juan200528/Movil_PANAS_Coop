// ComunidadesAdapter.java
package com.juan.movil_panas_coop.ui.comunidades;

import android.content.Context;
// Eliminar: import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Eliminar: import com.juan.movil_panas_coop.ChatActivity;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.model.Comunidad;

import java.util.List;

public class ComunidadesAdapter extends RecyclerView.Adapter<ComunidadesAdapter.ComunidadViewHolder> {

    private List<Comunidad> comunidadList;
    private Context context;
    private OnComunidadClickListener listener; // Interfaz para callback

    // Interfaz para manejar clics
    public interface OnComunidadClickListener {
        void onComunidadClick(Comunidad comunidad);
    }

    public ComunidadesAdapter(Context context, List<Comunidad> comunidadList, OnComunidadClickListener listener) {
        this.context = context;
        this.comunidadList = comunidadList;
        this.listener = listener; // Asignar el listener
    }

    @NonNull
    @Override
    public ComunidadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comunidad, parent, false);
        return new ComunidadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComunidadViewHolder holder, int position) {
        Comunidad comunidad = comunidadList.get(position);
        holder.textViewNombre.setText(comunidad.getNombre());
        if (comunidad.getDescripcion() != null && !comunidad.getDescripcion().isEmpty()) {
            holder.textViewDescripcion.setText(comunidad.getDescripcion());
            holder.textViewDescripcion.setVisibility(View.VISIBLE);
        } else {
            holder.textViewDescripcion.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComunidadClick(comunidad); // Notificar al fragmento
            }
        });
    }

    @Override
    public int getItemCount() {
        return comunidadList.size();
    }

    static class ComunidadViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNombre;
        TextView textViewDescripcion;

        public ComunidadViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.textViewComunidadNombre);
            textViewDescripcion = itemView.findViewById(R.id.textViewComunidadDescripcion);
        }
    }

    public void setComunidades(List<Comunidad> nuevasComunidades) {
        this.comunidadList.clear();
        this.comunidadList.addAll(nuevasComunidades);
        notifyDataSetChanged();
    }
}