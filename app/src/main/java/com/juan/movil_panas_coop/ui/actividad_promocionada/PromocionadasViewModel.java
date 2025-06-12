package com.juan.movil_panas_coop.ui.actividad_promocionada;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.juan.movil_panas_coop.api.RetrofitClient;
import com.juan.movil_panas_coop.api.ApiService;
import com.juan.movil_panas_coop.model.ActividadModel;
import com.juan.movil_panas_coop.models.Actividad;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromocionadasViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Actividad>> actividadesPromocionadas = new MutableLiveData<>();

    public PromocionadasViewModel(@NonNull Application application) {
        super(application);
        cargarActividadesPromocionadasDesdeApi();
    }

    public LiveData<List<Actividad>> getActividadesPromocionadas() {
        return actividadesPromocionadas;
    }

    private void cargarActividadesPromocionadasDesdeApi() {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.getPromotedTasks().enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Actividad> lista = new ArrayList<>();
                    for (ActividadModel modelo : response.body()) {
                        Actividad act = new Actividad();
                        act.setTitulo(modelo.getTitle());
                        act.setDescripcion(modelo.getDescription());
                        act.setFecha(modelo.getDate());
                        act.setLugar(modelo.getPlace());
                        act.setResponsables(String.join(", ", modelo.getResponsible()));
                        act.setImagenRuta(""); // Aquí puedes asignar una ruta si decides soportar imágenes remotas
                        lista.add(act);
                    }
                    actividadesPromocionadas.setValue(lista);
                } else {
                    actividadesPromocionadas.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                actividadesPromocionadas.setValue(new ArrayList<>());
            }
        });
    }

    /**
     * Nuevo método para llamar desde el Fragment cuando se active el Switch
     */
    public void cargarActividadesPromocionadas() {
        cargarActividadesPromocionadasDesdeApi();
    }
}