package com.juan.movil_panas_coop.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.movil_panas_coop.R;
import java.util.List;
import java.util.function.Consumer;

public class AsistenteAdapter extends RecyclerView.Adapter<AsistenteAdapter.AsistenteViewHolder> {

    private List<Asistente> asistenteList;
    private Consumer<Asistente> onEditClick;
    private Consumer<Asistente> onDeleteClick;

    public AsistenteAdapter(List<Asistente> asistenteList, Consumer<Asistente> onEditClick, Consumer<Asistente> onDeleteClick) {
        this.asistenteList = asistenteList;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public AsistenteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asistente, parent, false);
        return new AsistenteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AsistenteViewHolder holder, int position) {
        Asistente asistente = asistenteList.get(position);
        holder.tvNombreAsistente.setText(asistente.getNombre() != null ? asistente.getNombre() : "");
        holder.tvEmailAsistente.setText(asistente.getCorreoAbreviado()); // Usar correo abreviado
        holder.btnEditarAsistente.setOnClickListener(v -> onEditClick.accept(asistente));
        holder.btnEliminarAsistente.setOnClickListener(v -> onDeleteClick.accept(asistente));
    }

    @Override
    public int getItemCount() {
        return asistenteList.size();
    }

    static class AsistenteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreAsistente, tvEmailAsistente;
        ImageButton btnEditarAsistente, btnEliminarAsistente;

        public AsistenteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreAsistente = itemView.findViewById(R.id.tvNombreAsistente);
            tvEmailAsistente = itemView.findViewById(R.id.tvEmailAsistente);
            btnEditarAsistente = itemView.findViewById(R.id.btnEditarAsistente);
            btnEliminarAsistente = itemView.findViewById(R.id.btnEliminarAsistente);
        }
    }
}