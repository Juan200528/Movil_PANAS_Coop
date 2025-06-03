package com.juan.movil_panas_coop.ui.actividad_promocionada;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.model.Actividad;
import com.juan.movil_panas_coop.model.PromocionadaAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PromocionadasFragment extends Fragment implements PromocionadaAdapter.OnActividadClickListener {

    private RecyclerView recyclerPromocionadas;
    private TextView tvEmptyPromocionadas;
    private PromocionadaAdapter actividadAdapter;
    private PromocionadasViewModel viewModel;
    private List<Actividad> actividadList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_promocionadas, container, false);

        // Inicializar vistas
        recyclerPromocionadas = root.findViewById(R.id.recyclerPromocionadas);
        tvEmptyPromocionadas = root.findViewById(R.id.tvEmptyPromocionadas);

        // Configurar RecyclerView
        recyclerPromocionadas.setLayoutManager(new LinearLayoutManager(getContext()));
        actividadList = new ArrayList<>();
        actividadAdapter = new PromocionadaAdapter(actividadList, this, this::mostrarDialogoDetalles);
        recyclerPromocionadas.setAdapter(actividadAdapter);

        // Configurar ViewModel
        viewModel = new ViewModelProvider(this).get(PromocionadasViewModel.class);
        viewModel.getActividadesPromocionadas().observe(getViewLifecycleOwner(), actividades -> {
            actividadList.clear();
            actividadList.addAll((Collection<? extends Actividad>) actividades);
            actividadAdapter.notifyDataSetChanged();
            actualizarVisibilidad();
        });

        return root;
    }

    private void actualizarVisibilidad() {
        recyclerPromocionadas.setVisibility(actividadList.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmptyPromocionadas.setVisibility(actividadList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarDialogoDetalles(Actividad actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Ajustar el tamaño del diálogo
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        // Inicializar vistas del diálogo
        ImageView ivImagenDetalle = dialog.findViewById(R.id.ivImagenDetalle);
        TextView tvTituloDetalle = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDescripcionDetalle = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvFechaDetalle = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvLugarDetalle = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvResponsablesDetalle = dialog.findViewById(R.id.tvResponsablesDetalle);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);

        // Rellenar los campos con la información actual de la actividad
        tvTituloDetalle.setText(actividad.getTitulo());
        tvDescripcionDetalle.setText(actividad.getDescripcion());
        tvFechaDetalle.setText(actividad.getFecha());
        tvLugarDetalle.setText(actividad.getLugar());
        tvResponsablesDetalle.setText(actividad.getResponsables());

        // Cargar imagen desde archivo usando URI
        if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
            File imgFile = new File(actividad.getImagenRuta());
            if (imgFile.exists()) {
                ivImagenDetalle.setImageURI(Uri.fromFile(imgFile));
            }
        } else {
            ivImagenDetalle.setImageResource(R.drawable.default_image);
        }

        // Configurar listener para el botón Volver
        btnVolver.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onActividadClick(Actividad actividad) {
        Toast.makeText(getContext(), "Clic en actividad promocionada: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
    }
}