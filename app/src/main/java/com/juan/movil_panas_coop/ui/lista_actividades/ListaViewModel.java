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

    public void init(Context context, int userId) {
        cargarActividadesDesdeApi(context);
    }

    public LiveData<List<Actividad>> getActividades() {
        return actividades;
    }

    private void cargarActividadesDesdeApi(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String token = sessionManager.fetchAuthToken();

        if (token == null) {
            Log.e("ListaViewModel", "Token no encontrado en SessionManager");
            actividades.setValue(new ArrayList<>());
            return;
        }

        RetrofitClient.getApiService().obtenerActividadesOtrosUsuarios("Bearer " + token)
                .enqueue(new Callback<List<ActividadModel>>() {
                    @Override
                    public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Actividad> lista = new ArrayList<>();
                            for (ActividadModel model : response.body()) {
                                Actividad actividad = new Actividad();
                                actividad.setId(-1); // o puedes usar model.getId().hashCode() si deseas usarlo como identificador temporal
                                actividad.setTitulo(model.getTitle());
                                actividad.setDescripcion(model.getDescription());
                                actividad.setLugar(model.getPlace());
                                actividad.setFecha(model.getDate());
                                actividad.setResponsables(String.join(", ", model.getResponsible())); // lo convierte en String
                                actividad.setEstado("Activo"); // o lo que aplique por defecto
                                actividad.setPromocionada(false);
                                actividad.setPasada(false);
                                actividad.setAsistido(false);
                                actividad.setImagenRuta(null); // o asigna imagen por defecto si hay
                                lista.add(actividad);
                            }
                            actividades.setValue(lista);
                        } else {
                            Log.e("ListaViewModel", "Error al obtener actividades: " + response.code());
                            actividades.setValue(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                        Log.e("ListaViewModel", "Fallo de red al obtener actividades", t);
                        actividades.setValue(new ArrayList<>());
                    }
                });
    }
}
