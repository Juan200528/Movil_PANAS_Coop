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
import com.juan.movil_panas_coop.model.ActividadModel;
import com.juan.movil_panas_coop.models.Asistente;
import com.juan.movil_panas_coop.models.BuscarAdapter;
import com.juan.movil_panas_coop.models.PromocionadaViewModel;
import com.juan.movil_panas_coop.ui.recordatorio.RecordatorioFragment;

import java.util.ArrayList;
import java.util.List;

public class BuscarFragment extends Fragment implements
        BuscarAdapter.OnActividadClickListener,
        BuscarAdapter.OnDetallesClickListener,
        BuscarAdapter.OnAsistirClickListener,
        BuscarAdapter.OnConfigClickListener {

    private EditText etBuscar;
    private RadioGroup rgFechaSeleccionada, rgEstadoSeleccionado;
    private RadioButton rbFechaTodas, rbFechaProximas, rbFechaPasadas;
    private RadioButton rbEstadoTodas, rbEstadoPromocionadas;
    private Button btnBuscar;
    private RecyclerView recyclerViewActividades;
    private BuscarAdapter buscarAdapter;
    private List<ActividadModel> actividadList;
    private BuscarViewModel viewModel;
    private PromocionadaViewModel promocionadaViewModel;
    private ManagerDb managerDb;
    private int userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscar, container, false);

        // Inicialización de vistas
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

        // Inicializar lista y adaptador
        actividadList = new ArrayList<>();
        buscarAdapter = new BuscarAdapter(actividadList, this, this, this, this, userId, promocionadaViewModel);
        buscarAdapter.setManagerDb(managerDb);

        // Configurar RecyclerView
        recyclerViewActividades.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewActividades.setAdapter(buscarAdapter);
        recyclerViewActividades.setNestedScrollingEnabled(true);
        recyclerViewActividades.setHasFixedSize(false);
        recyclerViewActividades.setClipToPadding(false);
        recyclerViewActividades.setClipChildren(false);

        // Inicializar ViewModels
        viewModel = new ViewModelProvider(this).get(BuscarViewModel.class);
        promocionadaViewModel = new ViewModelProvider(this).get(PromocionadaViewModel.class);
        viewModel.init(getContext(), userId);
        promocionadaViewModel.init(viewModel.getSessionManager(), viewModel.getApiService());

        // Observar cambios en LiveData
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
            ViewGroup.LayoutParams params = recyclerViewActividades.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            recyclerViewActividades.setLayoutParams(params);

            float dpToPx = getResources().getDisplayMetrics().density;
            int maxItemHeight = (int) (600 * dpToPx); // Altura de item pasadas
            int marginPadding = (int) (106 * dpToPx); // Márgenes estimados
            int totalHeight = actividadList.size() * (maxItemHeight + marginPadding);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;
            int navigationBarHeight = getNavigationBarHeight();
            int bottomNavHeight = getResources().getDimensionPixelSize(R.dimen.bottom_navigation_height);
            int usableHeight = screenHeight - navigationBarHeight - bottomNavHeight;

            recyclerViewActividades.setNestedScrollingEnabled(totalHeight > usableHeight);
        }
    }

    private int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }

    @Override
    public void onActividadClick(ActividadModel actividad) {
        Log.d("BuscarFragment", "Clic en actividad: " + actividad.getTitle());
    }

    @Override
    public void onDetallesClick(ActividadModel actividad) {
        Log.d("BuscarFragment", "Ver detalles de actividad: " + actividad.getTitle());
    }

    @Override
    public void onAsistirClick(ActividadModel actividad, int position) {
        mostrarDialogoAsistir(actividad, position);
    }

    @Override
    public void onConfigClick(ActividadModel actividad) {
        Bundle bundle = new Bundle();
        bundle.putString("activity_id", actividad.getId());

        RecordatorioFragment recordatorioFragment = new RecordatorioFragment();
        recordatorioFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, recordatorioFragment)
                .addToBackStack(null)
                .commit();

        Log.d("BuscarFragment", "Navegando a RecordatorioFragment para: " + actividad.getTitle());
    }

    private void mostrarDialogoAsistir(ActividadModel actividad, int position) {
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
                    Toast.makeText(getContext(), "Correo inválido", Toast.LENGTH_SHORT).show();
                    return;
                }

                managerDb.open();
                Asistente asistente = new Asistente();
                asistente.setIdAsistente(userId);
                asistente.setIdActividad(Integer.parseInt(actividad.getId()));
                asistente.setNombreCompleto(nombreCompleto);
                asistente.setCorreo(email);
                asistente.setActividadNombre(actividad.getTitle());

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
                Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        ivCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}