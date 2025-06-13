package com.juan.movil_panas_coop.ui.busca_filtrar_actividades;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.db.ManagerDb;
import com.juan.movil_panas_coop.model.Actividad;
import com.juan.movil_panas_coop.model.Asistente;
import com.juan.movil_panas_coop.model.BuscarAdapter;
import java.util.ArrayList;
import java.util.List;

public class BuscarFragment extends Fragment implements BuscarAdapter.OnActividadClickListener,
        BuscarAdapter.OnDetallesClickListener, BuscarAdapter.OnAsistirClickListener {

    private EditText etBuscar;
    private RadioGroup rgFechaSeleccionada, rgEstadoSeleccionado;
    private RadioButton rbFechaTodas, rbFechaProximas, rbFechaPasadas;
    private RadioButton rbEstadoTodas, rbEstadoPromocionadas;
    private Button btnBuscar;
    private RecyclerView recyclerViewActividades;
    private BuscarAdapter buscarAdapter;
    private List<Actividad> actividadList;
    private BuscarViewModel viewModel;
    private ManagerDb managerDb;
    private int userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscar, container, false);

        etBuscar = view.findViewById(R.id.etBuscar);
        rgFechaSeleccionada = view.findViewById(R.id.rgFechaSeleccionada);
        rgEstadoSeleccionado = view.findViewById(R.id.rgEstadoSeleccionado);
        rbFechaTodas = view.findViewById(R.id.rbFechaTodas);
        rbFechaProximas = view.findViewById(R.id.rbFechaProximas);
        rbFechaPasadas = view.findViewById(R.id.rbFechaPasadas);
        rbEstadoTodas = view.findViewById(R.id.rbEstadoTodas);
        rbEstadoPromocionadas = view.findViewById(R.id.rbEstadoPromocionadas);
        btnBuscar = view.findViewById(R.id.btnBuscar);
        recyclerViewActividades = view.findViewById(R.id.recyclerViewActividades);

        // Inicializar ManagerDb y userId
        managerDb = new ManagerDb(getContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(getContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return view;
        }

        actividadList = new ArrayList<>();
        buscarAdapter = new BuscarAdapter(actividadList, this, this, this, userId);
        buscarAdapter.setManagerDb(managerDb);
        recyclerViewActividades.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewActividades.setAdapter(buscarAdapter);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(BuscarViewModel.class);
        viewModel.init(getContext(), userId);

        // Configurar RadioGroup para rgFechaSeleccionada
        rgFechaSeleccionada.setOnCheckedChangeListener((group, checkedId) -> {
            String fechaFiltro = "";
            if (rbFechaTodas.isChecked()) {
                fechaFiltro = "Todas";
            } else if (rbFechaProximas.isChecked()) {
                fechaFiltro = "Próximas";
            } else if (rbFechaPasadas.isChecked()) {
                fechaFiltro = "Pasadas";
            }
            viewModel.setFechaFiltro(fechaFiltro);
            Log.d("BuscarFragment", "Fecha seleccionada: " + fechaFiltro);
        });
        rbFechaTodas.setChecked(true);

        // Configurar RadioGroup para rgEstadoSeleccionado
        rgEstadoSeleccionado.setOnCheckedChangeListener((group, checkedId) -> {
            String estadoFiltro = "";
            if (rbEstadoTodas.isChecked()) {
                estadoFiltro = "Todas";
            } else if (rbEstadoPromocionadas.isChecked()) {
                estadoFiltro = "Promocionadas";
            }
            viewModel.setEstadoFiltro(estadoFiltro);
            Log.d("BuscarFragment", "Estado seleccionado: " + estadoFiltro);
        });
        rbEstadoTodas.setChecked(true);

        // Configurar el botón de búsqueda
        btnBuscar.setOnClickListener(v -> {
            String query = etBuscar.getText().toString().trim();
            viewModel.buscarActividades(query, "");
            Log.d("BuscarFragment", "Búsqueda iniciada con query: " + query);
        });

        // Observar los datos del ViewModel
        viewModel.getActividades().observe(getViewLifecycleOwner(), actividades -> {
            actividadList.clear();
            if (actividades != null) {
                actividadList.addAll(actividades);
            }
            buscarAdapter.notifyDataSetChanged();
            ajustarAlturaRecyclerView();
            Log.d("BuscarFragment", "Actividades actualizadas: " + actividadList.size());
        });

        return view;
    }

    private void ajustarAlturaRecyclerView() {
        if (recyclerViewActividades != null && recyclerViewActividades.getAdapter() != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;
            int navigationBarHeight = getNavigationBarHeight();
            int bottomNavHeight = getResources().getDimensionPixelSize(R.dimen.bottom_navigation_height);
            int usableHeight = screenHeight - navigationBarHeight - bottomNavHeight;

            float dpToPx = getResources().getDisplayMetrics().density;
            int activityItemHeight = (int) (150 * dpToPx); // Altura estimada por tarjeta de actividad
            int marginPadding = (int) (16 * dpToPx); // Margen/padding estimado entre ítems

            int totalHeight = actividadList.size() * (activityItemHeight + marginPadding);

            int maxHeight = usableHeight;
            totalHeight = Math.min(totalHeight, maxHeight);

            ViewGroup.LayoutParams params = recyclerViewActividades.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            recyclerViewActividades.setLayoutParams(params);
            recyclerViewActividades.setNestedScrollingEnabled(totalHeight > usableHeight);
            recyclerViewActividades.setClipToPadding(false);
        }
    }

    private int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    @Override
    public void onActividadClick(Actividad actividad) {
        Log.d("BuscarFragment", "Clic en actividad: " + actividad.getTitulo());
    }

    @Override
    public void onDetallesClick(Actividad actividad) {
        Log.d("BuscarFragment", "Ver detalles de actividad: " + actividad.getTitulo());
    }

    @Override
    public void onAsistirClick(Actividad actividad, int position) {
        mostrarDialogoAsistir(actividad, position);
    }

    private void mostrarDialogoAsistir(Actividad actividad, int position) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_asistir);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etNombreCompleto = dialog.findViewById(R.id.etNombreAsistir);
        EditText etEmail = dialog.findViewById(R.id.etEmailAsistir);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);
        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);

        managerDb.open();
        String[] datosUsuario = managerDb.obtenerDatosUsuarioPorId(userId);
        if (datosUsuario != null) {
            etNombreCompleto.setText(datosUsuario[1] != null ? datosUsuario[1] : "");
            etEmail.setText(datosUsuario[2] != null ? datosUsuario[2] : "");
        }
        managerDb.close();

        btnConfirmar.setOnClickListener(v -> {
            String nombreCompleto = etNombreCompleto.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (!nombreCompleto.isEmpty() && !email.isEmpty()) {
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(getContext(), "Por favor, ingresa un correo válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                managerDb.open();
                Asistente asistente = new Asistente();
                asistente.setIdAsistente(userId);
                asistente.setIdActividad(actividad.getId());
                asistente.setNombreCompleto(nombreCompleto);
                asistente.setCorreo(email);
                asistente.setActividadNombre(actividad.getTitulo());
                long result = managerDb.insertarAsistente(asistente);
                if (result != -1) {
                    actividad.setAsistido(true);
                    buscarAdapter.notifyItemChanged(position);
                    Toast.makeText(getContext(), "Asistencia confirmada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al registrar asistencia", Toast.LENGTH_SHORT).show();
                }
                managerDb.close();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        ivCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}