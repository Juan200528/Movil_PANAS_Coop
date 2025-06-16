package com.juan.movil_panas_coop.ui.actividad_promocionada;

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

public class PromocionadaViewModel extends ViewModel {
    private MutableLiveData<List<ActividadModel>> actividadesPromocionadas;
    private ApiService apiService;
    private SessionManager sessionManager;
    private Context context;

    public void init(Context context) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
        this.apiService = RetrofitClient.getApiService();
        if (actividadesPromocionadas == null) {
            actividadesPromocionadas = new MutableLiveData<>();
            cargarActividadesPromocionadas();
        }
    }

    public LiveData<List<ActividadModel>> getActividadesPromocionadas() {
        return actividadesPromocionadas;
    }

    public void cargarActividadesPromocionadas() {
        String token = "Bearer " + sessionManager.getToken();

        apiService.getPromotedTasks().enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    actividadesPromocionadas.setValue(response.body());
                } else {
                    Toast.makeText(context, "Error al cargar las actividades promocionadas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                Toast.makeText(context, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void promocionarActividad(String actividadId) {
        String token = "Bearer " + sessionManager.getToken();

        apiService.promoteTask(token, actividadId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Actividad promocionada exitosamente", Toast.LENGTH_SHORT).show();
                    cargarActividadesPromocionadas(); // Recargar la lista
                } else {
                    Toast.makeText(context, "Error al promocionar la actividad", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}