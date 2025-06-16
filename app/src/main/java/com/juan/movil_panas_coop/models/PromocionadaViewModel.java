package com.juan.movil_panas_coop.models;

import android.util.Log;
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
    private static final String TAG = "PromocionadaViewModel";
    private ApiService apiService;
    private SessionManager sessionManager;
    private MutableLiveData<List<ActividadModel>> actividadesPromocionadas = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public void init(SessionManager sessionManager, ApiService apiService) {
        this.sessionManager = sessionManager;
        this.apiService = apiService;
        cargarActividadesPromocionadas();
    }

    public LiveData<List<ActividadModel>> getActividadesPromocionadas() {
        return actividadesPromocionadas;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void cargarActividadesPromocionadas() {
        isLoading.setValue(true);
        String token = sessionManager.getAuthToken();

        apiService.getPromotedTasks("Bearer " + token).enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    actividadesPromocionadas.setValue(response.body());
                } else {
                    error.setValue("Error al cargar actividades promocionadas");
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Error de conexión: " + t.getMessage());
                Log.e(TAG, "Error al cargar actividades promocionadas", t);
            }
        });
    }

    public void promocionarActividad(String actividadId) {
        isLoading.setValue(true);
        String token = sessionManager.getAuthToken();

        apiService.promoteTask("Bearer " + token, actividadId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    cargarActividadesPromocionadas();
                } else {
                    error.setValue("Error al promocionar la actividad");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Error de conexión: " + t.getMessage());
                Log.e(TAG, "Error al promocionar actividad", t);
            }
        });
    }
}