package com.juan.movil_panas_coop.ui.busca_filtrar_actividades;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.juan.movil_panas_coop.db.ManagerDb;
import com.juan.movil_panas_coop.model.Actividad;
import java.util.List;

public class BuscarViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Actividad>> actividades = new MutableLiveData<>();
    private final MutableLiveData<String> fechaFiltro = new MutableLiveData<>("Todas");
    private final MutableLiveData<String> estadoFiltro = new MutableLiveData<>("Todas");
    private final ManagerDb managerDb;

    public BuscarViewModel(Application application) {
        super(application);
        managerDb = new ManagerDb(application);
        managerDb.open();
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
        List<Actividad> resultados = managerDb.buscarActividades(
                busqueda,
                fechaFiltro.getValue(),
                lugar,
                estadoFiltro.getValue()
        );
        actividades.setValue(resultados);
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
        managerDb.close();
    }
}