package com.juan.movil_panas_coop.ui.principal;

import android.app.Dialog;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.SnapHelper;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.models.Actividad;
import com.juan.movil_panas_coop.models.ActividadAdapter;
import com.juan.movil_panas_coop.db.ManagerDb;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.juan.movil_panas_coop.api.ApiService;
import com.juan.movil_panas_coop.api.RetrofitClient;
import com.juan.movil_panas_coop.model.ActividadModel;
import com.juan.movil_panas_coop.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrincipalFragment extends Fragment implements ActividadAdapter.OnActividadClickListener {

    private RecyclerView recyclerActividades;
    private TextView tvMisActividades;
    private TextView tvEmptyActividades;
    private ActividadAdapter actividadAdapter;
    private ManagerDb managerDb;
    private List<ActividadAdapter.Item> itemList;
    private int userId;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrincipalViewModel viewModel = new ViewModelProvider(this).get(PrincipalViewModel.class);
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_principal, container, false);

        recyclerActividades = root.findViewById(R.id.recyclerActividades);
        tvEmptyActividades = root.findViewById(R.id.tvEmptyActividades);

        managerDb = new ManagerDb(getContext());
        managerDb.open();

        SessionManager sessionManager = new SessionManager(requireContext());
        String userIdString = sessionManager.getUserId();
        if (userIdString != null) {
            try {
                userId = Integer.parseInt(userIdString);
                Log.d("PrincipalFragment", "User ID retrieved: " + userId);
            } catch (NumberFormatException e) {
                Log.e("PrincipalFragment", "Error parsing user_id: " + userIdString, e);
                Toast.makeText(getContext(), "Error: Invalid user ID format.", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e("PrincipalFragment", "ERROR: user_id not found in SessionManager.");
            Toast.makeText(getContext(), "Error: No user ID found. Please log in again.", Toast.LENGTH_LONG).show();
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerActividades.setLayoutManager(layoutManager);
        recyclerActividades.setHasFixedSize(true);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerActividades);

        itemList = new ArrayList<>();
        actividadAdapter = new ActividadAdapter(itemList, this, this::mostrarDialogoEliminar,
                this::mostrarDialogoEditar, this::mostrarDialogoDetalles);
        actividadAdapter.setManagerDb(managerDb);
        recyclerActividades.setAdapter(actividadAdapter);

        cargarActividades();

        return root;
    }


    private void adjustRecyclerViewMargin() {
        if (recyclerActividades != null) {
            // Obtener la altura de la barra de navegación
            int navigationBarHeight = getNavigationBarHeight();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) recyclerActividades.getLayoutParams();
            params.bottomMargin = navigationBarHeight + 150; // Mantener el margen inferior ajustado a 150
            recyclerActividades.setLayoutParams(params);

            // Asegurar que el RecyclerView no se corte por la barra de navegación
            ViewCompat.setOnApplyWindowInsetsListener(recyclerActividades, (v, insets) -> {
                int insetBottom = insets.getSystemWindowInsetBottom();
                if (insetBottom > 0) {
                    params.bottomMargin = insetBottom + 150;
                    recyclerActividades.setLayoutParams(params);
                }
                return insets.consumeSystemWindowInsets();
            });
        }
    }

    private int getNavigationBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        requireActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        return realHeight > usableHeight ? realHeight - usableHeight : 0;
    }



    public void cargarActividades() {
        Log.d("PrincipalFragment", "=== INICIO cargarActividades() desde API ===");

        SessionManager sessionManager = new SessionManager(requireContext());
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Log.e("PrincipalFragment", "No hay token de autenticación");
            Toast.makeText(getContext(), "Error: No se encontró token de autenticación", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService api = RetrofitClient.getApiService();
        Call<List<ActividadModel>> call = api.obtenerActividades("Bearer " + token);

        call.enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ActividadModel> actividadesAPI = response.body();
                    Log.d("PrincipalFragment", "Actividades recibidas de API: " + actividadesAPI.size());

                    procesarActividadesDeAPI(actividadesAPI);
                } else {
                    Log.e("PrincipalFragment", "Error al obtener actividades: " + response.code());
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error al cargar actividades", Toast.LENGTH_SHORT).show();
                        actualizarVisibilidad();
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                Log.e("PrincipalFragment", "Error de conexión: " + t.getMessage());
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                    actualizarVisibilidad();
                });
            }
        });
    }

    private void procesarActividadesDeAPI(List<ActividadModel> actividadesAPI) {
        executorService.execute(() -> {
            List<ActividadAdapter.Item> tempItemList = new ArrayList<>();

            List<Actividad> actividadesConvertidas = new ArrayList<>();
            for (ActividadModel actividadAPI : actividadesAPI) {
                Actividad actividad = convertirAPIaActividad(actividadAPI);
                actividadesConvertidas.add(actividad);
                Log.d("PrincipalFragment", "Actividad convertida: " + actividad.getTitulo() + " - " + actividad.getFecha());
            }

            if (!actividadesConvertidas.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String fechaHoyStr = sdf.format(new Date());
                Date fechaHoy;
                try {
                    fechaHoy = sdf.parse(fechaHoyStr);
                } catch (ParseException e) {
                    fechaHoy = new Date();
                }

                List<Actividad> actividadesActuales = new ArrayList<>();
                List<Actividad> actividadesPasadas = new ArrayList<>();

                for (Actividad actividad : actividadesConvertidas) {
                    if (actividad.getFecha() == null || actividad.getFecha().isEmpty()) {
                        actividad.setPasada(false);
                        actividadesActuales.add(actividad);
                        continue;
                    }

                    try {
                        Date actividadDate = sdf.parse(actividad.getFecha());
                        if (actividadDate.before(fechaHoy)) {
                            actividad.setPasada(true);
                            actividadesPasadas.add(actividad);
                        } else {
                            actividad.setPasada(false);
                            actividadesActuales.add(actividad);
                        }
                    } catch (ParseException e) {
                        Log.e("PrincipalFragment", "Error parsing date: " + actividad.getFecha(), e);
                        actividad.setPasada(false);
                        actividadesActuales.add(actividad);
                    }
                }

                Collections.sort(actividadesActuales, (a1, a2) -> {
                    try {
                        Date fecha1 = sdf.parse(a1.getFecha());
                        Date fecha2 = sdf.parse(a2.getFecha());
                        return fecha1.compareTo(fecha2);
                    } catch (ParseException e) {
                        return 0;
                    }
                });

                for (Actividad actividad : actividadesActuales) {
                    tempItemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_ACTIVIDAD, actividad, null, null));
                }

                if (!actividadesPasadas.isEmpty()) {
                    Collections.sort(actividadesPasadas, (a1, a2) -> {
                        try {
                            Date fecha1 = sdf.parse(a1.getFecha());
                            Date fecha2 = sdf.parse(a2.getFecha());
                            return fecha2.compareTo(fecha1);
                        } catch (ParseException e) {
                            return 0;
                        }
                    });

                    tempItemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_TITULO, null,
                            getString(R.string.actividades_pasadas).toUpperCase(Locale.getDefault()), null));
                    tempItemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_PASADAS, null, null, actividadesPasadas));
                }
            }

            requireActivity().runOnUiThread(() -> {
                itemList.clear();
                itemList.addAll(tempItemList);
                Log.d("PrincipalFragment", "Total de ítems cargados desde API: " + itemList.size());

                actividadAdapter.notifyDataSetChanged();
                actualizarVisibilidad();

                if (!itemList.isEmpty()) {
                    recyclerActividades.scrollToPosition(0);
                    adjustScrollBehavior();
                }
            });
        });
    }

    private Actividad convertirAPIaActividad(ActividadModel actividadAPI) {
        Actividad actividad = new Actividad();
        actividad.setId(actividadAPI.getId() != null ? Integer.parseInt(actividadAPI.getId()) : 0);
        actividad.setTitulo(actividadAPI.getTitle());
        actividad.setDescripcion(actividadAPI.getDescription());
        actividad.setLugar(actividadAPI.getPlace());
        actividad.setIdCreador(userId);

        if (actividadAPI.getDate() != null) {
            actividad.setFecha(convertirFechaISOaLocal(actividadAPI.getDate()));
        }

        if (actividadAPI.getResponsible() != null && !actividadAPI.getResponsible().isEmpty()) {
            actividad.setResponsables(String.join(", ", actividadAPI.getResponsible()));
        }

        return actividad;
    }

    private String convertirFechaISOaLocal(String fechaISO) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat localFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = isoFormat.parse(fechaISO);
            return localFormat.format(date);
        } catch (ParseException e) {
            Log.e("PrincipalFragment", "Error convirtiendo fecha ISO con milisegundos: " + fechaISO, e);
            try {
                SimpleDateFormat isoFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                isoFormat2.setTimeZone(TimeZone.getTimeZone("UTC"));
                SimpleDateFormat localFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = isoFormat2.parse(fechaISO);
                return localFormat.format(date);
            } catch (ParseException e2) {
                Log.e("PrincipalFragment", "Error convirtiendo fecha ISO sin milisegundos: " + fechaISO, e2);
                try {
                    SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat localFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date date = simpleFormat.parse(fechaISO);
                    return localFormat.format(date);
                } catch (ParseException e3) {
                    Log.e("PrincipalFragment", "Error convirtiendo fecha simple: " + fechaISO, e3);
                    return fechaISO;
                }
            }
        }
    }


    private void adjustScrollBehavior() {
        if (recyclerActividades != null && recyclerActividades.getAdapter() != null) {
            int itemCount = recyclerActividades.getAdapter().getItemCount();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;
            int navigationBarHeight = getNavigationBarHeight();
            int usableHeight = screenHeight - navigationBarHeight;

            // Estimar la altura de una tarjeta (aproximadamente 150dp + márgenes/padding)
            float dpToPx = getResources().getDisplayMetrics().density;
            int itemHeight = (int) (150 * dpToPx) + 32; // 150dp + 16dp de margen superior + 16dp de margen inferior

            int totalHeight = itemCount * itemHeight;

            // Si hay una sola actividad y su altura es menor o igual a la altura usable, deshabilitar scroll
            if (itemCount <= 1 && totalHeight <= usableHeight) {
                recyclerActividades.setNestedScrollingEnabled(false); // Deshabilitar scroll
                ViewGroup.LayoutParams params = recyclerActividades.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT; // Ajustar altura al contenido
                recyclerActividades.setLayoutParams(params);
            } else {
                recyclerActividades.setNestedScrollingEnabled(true); // Habilitar scroll
                ViewGroup.LayoutParams params = recyclerActividades.getLayoutParams();
                params.height = ViewGroup.LayoutParams.MATCH_PARENT; // Restaurar altura completa
                recyclerActividades.setLayoutParams(params);
            }
        }
    }

    private void actualizarVisibilidad() {
        recyclerActividades.setVisibility(itemList.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmptyActividades.setVisibility(itemList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarDialogoEliminar(Actividad actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_eliminar_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);

        ivCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnConfirmar.setOnClickListener(v -> {
            managerDb.eliminarActividad(actividad.getId());
            cargarActividades();
            Toast.makeText(getContext(), "Actividad eliminada", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoEditar(Actividad actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_editar_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);
        EditText etEditarTitulo = dialog.findViewById(R.id.etEditarTitulo);
        EditText etEditarDescripcion = dialog.findViewById(R.id.etEditarDescripcion);
        EditText etEditarFecha = dialog.findViewById(R.id.etEditarFecha);
        EditText etEditarLugar = dialog.findViewById(R.id.etEditarLugar);
        EditText etEditarResponsables = dialog.findViewById(R.id.etEditarResponsables);
        Button btnGuardarCambios = dialog.findViewById(R.id.btnGuardarCambios);

        etEditarTitulo.setText(actividad.getTitulo() != null ? actividad.getTitulo() : "");
        etEditarDescripcion.setText(actividad.getDescripcion() != null ? actividad.getDescripcion() : "");
        etEditarFecha.setText(actividad.getFecha() != null ? actividad.getFecha() : "");
        etEditarLugar.setText(actividad.getLugar() != null ? actividad.getLugar() : "");
        etEditarResponsables.setText(actividad.getResponsables() != null ? actividad.getResponsables() : "");

        ivCerrar.setOnClickListener(v -> dialog.dismiss());
        btnGuardarCambios.setOnClickListener(v -> {
            actividad.setTitulo(etEditarTitulo.getText().toString());
            actividad.setDescripcion(etEditarDescripcion.getText().toString());
            String nuevaFecha = etEditarFecha.getText().toString();
            actividad.setFecha(nuevaFecha);
            actividad.setLugar(etEditarLugar.getText().toString());
            actividad.setResponsables(etEditarResponsables.getText().toString());

            // Actualizar el estado de pasada después de editar la fecha
            try {
                Date currentDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date actividadDate = sdf.parse(nuevaFecha);
                actividad.setPasada(actividadDate.before(currentDate));
            } catch (ParseException e) {
                Log.e("PrincipalFragment", "Error al parsear la nueva fecha: " + nuevaFecha, e);
                actividad.setPasada(false); // Por defecto, no pasada si hay error
            }

            managerDb.actualizarActividad(actividad);
            cargarActividades();
            Toast.makeText(getContext(), "Actividad actualizada", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoDetalles(Actividad actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        ImageView ivImagenDetalle = dialog.findViewById(R.id.ivImagenDetalle);
        TextView tvTituloDetalle = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDescripcionDetalle = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvFechaDetalle = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvLugarDetalle = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvResponsablesDetalle = dialog.findViewById(R.id.tvResponsablesDetalle);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);

        tvTituloDetalle.setText(actividad.getTitulo() != null ? actividad.getTitulo() : "Sin título");
        tvDescripcionDetalle.setText(actividad.getDescripcion() != null ? actividad.getDescripcion() : "Sin descripción");
        tvFechaDetalle.setText(actividad.getFecha() != null ? actividad.getFecha() : "Sin fecha");
        tvLugarDetalle.setText(actividad.getLugar() != null ? actividad.getLugar() : "Sin lugar");
        tvResponsablesDetalle.setText(actividad.getResponsables() != null ? actividad.getResponsables() : "Sin responsables");

        String imagenRuta = actividad.getImagenRuta();
        if (imagenRuta != null && !imagenRuta.isEmpty()) {
            File imgFile = new File(imagenRuta);
            if (imgFile.exists()) {
                ivImagenDetalle.setImageURI(Uri.fromFile(imgFile));
            } else {
                ivImagenDetalle.setImageResource(R.drawable.default_image);
            }
        } else {
            ivImagenDetalle.setImageResource(R.drawable.default_image);
        }

        btnVolver.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (managerDb != null) {
            managerDb.close();
        }
        executorService.shutdown();
    }

    @Override
    public void onActividadClick(Actividad actividad) {
        Toast.makeText(getContext(), "Clic en actividad: " + (actividad.getTitulo() != null ? actividad.getTitulo() : "Sin título"), Toast.LENGTH_SHORT).show();
    }
}