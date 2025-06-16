package com.juan.movil_panas_coop.ui.lista_actividades;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.juan.movil_panas_coop.api.RetrofitClient;
import com.juan.movil_panas_coop.model.ActividadModel;
import com.juan.movil_panas_coop.models.Actividad;
import com.juan.movil_panas_coop.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Context;

public class ListaViewModel extends ViewModel {
    private final MutableLiveData<List<Actividad>> actividades = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public void init(Context context, int userId) {
        cargarActividadesDesdeApi(context);
    }

    public LiveData<List<Actividad>> getActividades() {
        return actividades;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    private void cargarActividadesDesdeApi(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String token = sessionManager.fetchAuthToken();

        if (token == null) {
            error.setValue("Token no encontrado. Por favor, inicia sesión nuevamente.");
            return;
        }

        isLoading.setValue(true);
        RetrofitClient.getApiService().obtenerActividadesOtrosUsuarios("Bearer " + token)
                .enqueue(new Callback<List<ActividadModel>>() {
                    @Override
                    public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                        isLoading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Actividad> lista = convertirActividadesModelAActividades(response.body());
                            actividades.setValue(lista);
                        } else {
                            error.setValue("Error al obtener actividades: " + 
                                (response.errorBody() != null ? response.errorBody().toString() : "Código " + response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                        isLoading.setValue(false);
                        error.setValue("Error de conexión: " + t.getMessage());
                    }
                });
    }

    private List<Actividad> convertirActividadesModelAActividades(List<ActividadModel> actividadesModel) {
        List<Actividad> lista = new ArrayList<>();
        for (ActividadModel model : actividadesModel) {
            Actividad actividad = new Actividad();
            actividad.setId(Integer.parseInt(model.getId())); // Convertir String a int
            actividad.setTitulo(model.getTitle());
            actividad.setDescripcion(model.getDescription());
            actividad.setLugar(model.getPlace());
            actividad.setFecha(model.getDate());
            actividad.setResponsables(String.join(", ", model.getResponsible()));
            actividad.setEstado(model.getStatus());
            actividad.setPromocionada(model.isPromoted());
            actividad.setPasada(false);
            actividad.setAsistido(false);
            actividad.setImagenRuta(model.getImageUrl());
            lista.add(actividad);
        }
        return lista;
    }
}
