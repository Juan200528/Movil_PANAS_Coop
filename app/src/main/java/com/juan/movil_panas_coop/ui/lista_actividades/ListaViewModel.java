package com.juan.movil_panas_coop.ui.lista_actividades;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.juan.movil_panas_coop.db.ManagerDb;
import com.juan.movil_panas_coop.models.Actividad;
import java.util.List;

public class ListaViewModel extends ViewModel {

    private final MutableLiveData<List<Actividad>> actividades = new MutableLiveData<>();
    private ManagerDb managerDb;

    public void init(Context context, int userId) {
        managerDb = new ManagerDb(context);
        managerDb.open();
        cargarActividades(userId);
    }

    public LiveData<List<Actividad>> getActividades() {
        return actividades;
    }

    public void cargarActividades(int userId) {
        actividades.setValue(managerDb.obtenerActividadesOtrosUsuarios(userId));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (managerDb != null) {
            managerDb.close();
        }
    }
}