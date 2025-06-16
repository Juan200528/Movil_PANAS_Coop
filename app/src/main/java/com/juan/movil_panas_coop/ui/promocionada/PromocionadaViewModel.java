package com.juan.movil_panas_coop.ui.promocionada;

import android.widget.Toast;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.juan.movil_panas_coop.api.ApiService;
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

    public void init(android.content.Context context) {
        actividadesPromocionadas = new MutableLiveData<>();
        sessionManager = new SessionManager(context);
        apiService = new ApiService();
        cargarActividadesPromocionadas();
    }

    public LiveData<List<ActividadModel>> getActividadesPromocionadas() {
        return actividadesPromocionadas;
    }

    private void cargarActividadesPromocionadas() {
        String token = "Bearer " + sessionManager.getAuthToken();
        apiService.getPromotedTasks(token).enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    actividadesPromocionadas.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                // Manejar error
            }
        });
    }

    public void promocionarActividad(String actividadId) {
        String token = "Bearer " + sessionManager.getAuthToken();
        apiService.promoteTask(token, actividadId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cargarActividadesPromocionadas(); // Recargar la lista
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Manejar error
            }
        });
    }
}