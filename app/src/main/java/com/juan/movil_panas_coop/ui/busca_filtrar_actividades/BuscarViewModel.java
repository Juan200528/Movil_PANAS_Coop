package com.juan.movil_panas_coop.ui.busca_filtrar_actividades;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.juan.movil_panas_coop.db.ManagerDb;
import com.juan.movil_panas_coop.models.Actividad;
import java.util.List;

public class BuscarViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Actividad>> actividades = new MutableLiveData<>();
    private final MutableLiveData<String> fechaFiltro = new MutableLiveData<>("Todas");
    private final MutableLiveData<String> estadoFiltro = new MutableLiveData<>("Todas");
    private ManagerDb managerDb;
    private int userId;

    public BuscarViewModel(Application application) {
        super(application);
        managerDb = new ManagerDb(application);
        managerDb.open();
    }

    public void init(Context context, int userId) {
        this.userId = userId;
        managerDb = new ManagerDb(context);
        managerDb.open();
        cargarActividades();
    }

    public LiveData<List<Actividad>> getActividades() {
        return actividades;
    }

    public LiveData<String> getFechaFiltro() {
        return fechaFiltro;
    }

    public LiveData<String> getEstadoFiltro() {
        return estadoFiltro;
    }

    public void buscarActividades(String busqueda, String lugar) {
        String fechaFiltroValue = fechaFiltro.getValue();
        String estadoFiltroValue = estadoFiltro.getValue();

        // Ajustar filtros para "Todas"
        if ("Todas".equals(fechaFiltroValue)) {
            fechaFiltroValue = "";
        }
        if ("Todas".equals(estadoFiltroValue)) {
            estadoFiltroValue = "";
        }

        List<Actividad> resultados = managerDb.buscarActividades(
                busqueda,
                fechaFiltroValue,
                lugar,
                estadoFiltroValue
        );
        actividades.setValue(resultados);
    }

    public void cargarActividades() {
        actividades.setValue(managerDb.obtenerActividadesOtrosUsuarios(userId));
    }

    public void setFechaFiltro(String filtro) {
        fechaFiltro.setValue(filtro);
    }

    public void setEstadoFiltro(String filtro) {
        estadoFiltro.setValue(filtro);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (managerDb != null) {
            managerDb.close();
        }
    }
}