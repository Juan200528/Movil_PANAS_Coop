package com.juan.movil_panas_coop.ui.recordatorio;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.juan.movil_panas_coop.db.ManagerDb;
import com.juan.movil_panas_coop.models.Notificacion;
import java.util.List;

public class RecordatorioViewModel extends AndroidViewModel {
    private ManagerDb managerDb;
    private MutableLiveData<List<Notificacion>> notificaciones;

    public RecordatorioViewModel(Application application) {
        super(application);
        managerDb = new ManagerDb(application);
        managerDb.open();
        notificaciones = new MutableLiveData<>();
    }

    public LiveData<List<Notificacion>> getNotificaciones() {
        return notificaciones;
    }

    public void cargarNotificaciones(int userId) {
        List<Notificacion> notificacionList = managerDb.obtenerNotificacionesPorUsuario(userId);
        notificaciones.setValue(notificacionList);
    }

    public void insertarNotificacion(Notificacion notificacion) {
        managerDb.insertarNotificacion(notificacion);
        actualizarNotificaciones(notificacion.getIdUsuario());
    }

    public void actualizarNotificaciones(int userId) {
        managerDb.actualizarDiasRestantes();
        List<Notificacion> notificacionList = managerDb.obtenerNotificacionesPorUsuario(userId);
        notificaciones.setValue(notificacionList);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        managerDb.close();
    }
}