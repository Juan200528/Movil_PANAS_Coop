package com.juan.movil_panas_coop.ui.busca_filtrar_actividades;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.juan.movil_panas_coop.api.ApiService;
import com.juan.movil_panas_coop.api.RetrofitClient;
import com.juan.movil_panas_coop.model.ActividadModel;
import com.juan.movil_panas_coop.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscarViewModel extends ViewModel {
    private MutableLiveData<List<ActividadModel>> actividades;
    private ApiService apiService;
    private SessionManager sessionManager;
    private Context context;
    private int userId;

    public void init(Context context, int userId) {
        this.context = context;
        this.userId = userId;
        this.sessionManager = new SessionManager(context);
        this.apiService = RetrofitClient.getApiService();
        if (actividades == null) {
            actividades = new MutableLiveData<>();
            cargarActividades();
        }
    }

    public LiveData<List<ActividadModel>> getActividades() {
        return actividades;
    }

    public void cargarActividades() {
        String token = "Bearer " + sessionManager.getToken();

        apiService.obtenerActividadesOtrosUsuarios(token).enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    actividades.setValue(response.body());
                } else {
                    Toast.makeText(context, "Error al cargar las actividades", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                Toast.makeText(context, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void buscarActividades(String query, String filtroFecha, String filtroEstado) {
        String token = "Bearer " + sessionManager.getToken();

        // Por ahora, cargamos todas las actividades y filtramos localmente
        // TODO: Implementar filtros en el backend
        apiService.obtenerActividadesOtrosUsuarios(token).enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ActividadModel> todasActividades = response.body();
                    // Aquí puedes implementar la lógica de filtrado local
                    actividades.setValue(todasActividades);
                } else {
                    Toast.makeText(context, "Error al buscar actividades", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                Toast.makeText(context, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}