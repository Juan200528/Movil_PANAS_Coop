package com.juan.movil_panas_coop.ui.actividad_promocionada;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.api.ApiService;
import com.juan.movil_panas_coop.api.RetrofitClient;
import com.juan.movil_panas_coop.model.ActividadModel;
import com.juan.movil_panas_coop.models.ActividadListaAdapter;
import com.juan.movil_panas_coop.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class PromocionadaFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ActividadListaAdapter adapter;
    private PromocionadaViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_promocionada, container, false);

        // Inicializar vistas
        recyclerView = root.findViewById(R.id.recyclerPromocionadas);
        tvEmpty = root.findViewById(R.id.tvEmptyPromocionadas);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(PromocionadaViewModel.class);
        viewModel.init(requireContext().getApplicationContext());

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActividadListaAdapter(new ArrayList<>(), viewModel);
        recyclerView.setAdapter(adapter);

        // Observar cambios en las actividades promocionadas
        viewModel.getActividadesPromocionadas().observe(getViewLifecycleOwner(), actividades -> {
            if (actividades != null && !actividades.isEmpty()) {
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setActividades(actividades);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        return root;
    }
}