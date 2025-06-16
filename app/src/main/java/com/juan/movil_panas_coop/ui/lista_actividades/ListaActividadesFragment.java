package com.juan.movil_panas_coop.ui.lista_actividades;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.api.ApiService;
import com.juan.movil_panas_coop.api.RetrofitClient;
import com.juan.movil_panas_coop.model.ActividadModel;
import com.juan.movil_panas_coop.models.ActividadAdapter;
import com.juan.movil_panas_coop.models.ActividadListaAdapter;
import com.juan.movil_panas_coop.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaActividadesFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ActividadListaAdapter adapter;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lista, container, false);

        // Inicializar vistas
        recyclerView = root.findViewById(R.id.recyclerLista);
        tvEmpty = root.findViewById(R.id.tvEmptyLista);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActividadListaAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Inicializar servicios
        sessionManager = new SessionManager(requireContext());
        apiService = RetrofitClient.getApiService();

        // Cargar actividades
        cargarActividades();

        return root;
    }

    private void cargarActividades() {
        String token = "Bearer " + sessionManager.getToken();

        apiService.obtenerActividades(token).enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ActividadModel> actividades = response.body();
                    if (actividades.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setActividades(actividades);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar las actividades", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}