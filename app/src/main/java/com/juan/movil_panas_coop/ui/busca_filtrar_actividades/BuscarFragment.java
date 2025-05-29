package com.juan.movil_panas_coop.ui.busca_filtrar_actividades;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.db.ManagerDb;
import com.juan.movil_panas_coop.model.Actividad;
import com.juan.movil_panas_coop.model.BuscarAdapter;
import java.util.ArrayList;
import java.util.List;

public class BuscarFragment extends Fragment implements BuscarAdapter.OnActividadClickListener,
        BuscarAdapter.OnEliminarClickListener, BuscarAdapter.OnEditarClickListener {

    private EditText etBuscar, etLugar;
    private Spinner tvFechaSeleccionada, tvEstadoSeleccionado;
    private Button btnBuscar;
    private RecyclerView recyclerViewActividades;
    private BuscarAdapter buscarAdapter;
    private List<Actividad> actividadList;
    private BuscarViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscar, container, false);

        etBuscar = view.findViewById(R.id.etBuscar);
        etLugar = view.findViewById(R.id.etLugar);
        tvFechaSeleccionada = view.findViewById(R.id.tvFechaSeleccionada);
        tvEstadoSeleccionado = view.findViewById(R.id.tvEstadoSeleccionado);
        btnBuscar = view.findViewById(R.id.btnBuscar);
        recyclerViewActividades = view.findViewById(R.id.recyclerViewActividades);

        actividadList = new ArrayList<>();
        buscarAdapter = new BuscarAdapter(actividadList, this, this, this);
        recyclerViewActividades.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewActividades.setAdapter(buscarAdapter);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(BuscarViewModel.class);

        // Configurar Spinner para tvFechaSeleccionada
        ArrayAdapter<String> fechaAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, new String[]{"Todas", "Próximas", "Pasadas"});
        fechaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tvFechaSeleccionada.setAdapter(fechaAdapter);
        tvFechaSeleccionada.setSelection(0);
        tvFechaSeleccionada.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String fechaFiltro = parent.getItemAtPosition(position).toString();
                viewModel.setFechaFiltro(fechaFiltro);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // No hacer nada
            }
        });
        Log.d("BuscarFragment", "Spinner tvFechaSeleccionada configurado con opciones: Todas, Próximas, Pasadas");

        // Configurar Spinner para tvEstadoSeleccionado
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, new String[]{"Todas", "Promocionadas"});
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tvEstadoSeleccionado.setAdapter(estadoAdapter);
        tvEstadoSeleccionado.setSelection(0);
        tvEstadoSeleccionado.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String estadoFiltro = parent.getItemAtPosition(position).toString();
                viewModel.setEstadoFiltro(estadoFiltro);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // No hacer nada
            }
        });
        Log.d("BuscarFragment", "Spinner tvEstadoSeleccionado configurado con opciones: Todas, Promocionadas");

        // Configurar el botón btnBuscar para realizar la búsqueda solo si hay datos
        btnBuscar.setOnClickListener(v -> {
            String query = etBuscar.getText().toString().trim();
            String lugar = etLugar.getText().toString().trim();

            if (query.isEmpty() && lugar.isEmpty()) {
                actividadList.clear();
                buscarAdapter.notifyDataSetChanged();
                Log.d("BuscarFragment", "No se realizó búsqueda: Campos vacíos");
                Toast.makeText(getContext(), "Ingresa un criterio de búsqueda", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("BuscarFragment", "Realizando búsqueda. Query: " + query + ", Lugar: " + lugar +
                        ", Fecha: " + viewModel.getFechaFiltro().getValue() + ", Estado: " + viewModel.getEstadoFiltro().getValue());
                viewModel.buscarActividades(query, lugar);
            }
        });

        // Desactivar la observación automática inicial
        viewModel.getActividades().observe(getViewLifecycleOwner(), actividades -> {
            if (actividades != null) {
                actividadList.clear();
                actividadList.addAll(actividades);
                buscarAdapter.notifyDataSetChanged();
                Log.d("BuscarFragment", "Actividades actualizadas: " + actividades.size());
                for (Actividad actividad : actividades) {
                    Log.d("BuscarFragment", "Actividad: " + actividad.getTitulo() + ", Fecha: " + actividad.getFecha() + ", Pasada: " + actividad.isPasada());
                }
            }
        });

        // Inicialmente limpiar la lista para evitar que se muestren datos previos
        actividadList.clear();
        buscarAdapter.notifyDataSetChanged();

        return view;
    }

    @Override
    public void onActividadClick(Actividad actividad) {
        Log.d("BuscarFragment", "Clic en actividad: " + actividad.getTitulo());
    }

    @Override
    public void onEliminarClick(Actividad actividad) {
        Log.d("BuscarFragment", "Eliminar actividad: " + actividad.getTitulo());
        ManagerDb db = new ManagerDb(getContext());
        db.open();
        db.eliminarActividad(actividad.getId());
        db.close();
        viewModel.buscarActividades(etBuscar.getText().toString().trim(), etLugar.getText().toString().trim()); // Actualizar búsqueda
    }

    @Override
    public void onEditarClick(Actividad actividad) {
        Log.d("BuscarFragment", "Editar actividad: " + actividad.getTitulo());
    }
}