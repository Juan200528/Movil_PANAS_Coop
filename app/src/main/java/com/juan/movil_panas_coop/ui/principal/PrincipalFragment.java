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
import com.juan.movil_panas_coop.model.Actividad;
import com.juan.movil_panas_coop.model.ActividadAdapter;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrincipalFragment extends Fragment implements ActividadAdapter.OnActividadClickListener {

    private RecyclerView recyclerActividades;
    private TextView tvMisActividades;
    private TextView tvEmptyActividades;
    private ActividadAdapter actividadAdapter;
    private ManagerDb managerDb;
    private List<ActividadAdapter.Item> itemList;
    private int userId;
    private ExecutorService executorService;

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
        tvMisActividades = root.findViewById(R.id.tvMisActividades);
        tvEmptyActividades = root.findViewById(R.id.tvEmptyActividades);

        managerDb = new ManagerDb(getContext());
        managerDb.open();

        userId = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE)
                .getInt("user_id", -1);
        Log.d("PrincipalFragment", "User ID in PrincipalFragment: " + userId);
        if (userId == -1) {
            Log.e("PrincipalFragment", "ERROR: user_id is -1 in SharedPreferences. Check login flow.");
            Toast.makeText(getContext(), "Error: No se encontró el ID de usuario. Verifique el inicio de sesión.", Toast.LENGTH_LONG).show();
        }

        // Configurar el LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerActividades.setLayoutManager(layoutManager);
        recyclerActividades.setHasFixedSize(true); // Optimizar con tamaño fijo

        // Agregar SnapHelper para alinear las tarjetas completamente
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerActividades);

        // Ajustar el margen inferior para evitar superposición con la barra de navegación
        adjustRecyclerViewMargin();

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
        executorService.execute(() -> {
            List<ActividadAdapter.Item> tempItemList = new ArrayList<>();

            if (userId != -1) {
                // Fetch all non-past activities for the user
                List<Actividad> allActividades = managerDb.obtenerActividadesPorUsuario(userId);
                Log.d("PrincipalFragment", "Total actividades recuperadas para userId " + userId + ": " + allActividades.size());
                for (Actividad actividad : allActividades) {
                    Log.d("PrincipalFragment", "Actividad - ID: " + actividad.getId() +
                            ", Título: " + actividad.getTitulo() +
                            ", Fecha: " + actividad.getFecha() +
                            ", idCreador: " + actividad.getIdCreador() +
                            ", isPasada: " + actividad.isPasada() +
                            ", Estado: " + actividad.getEstado() +
                            ", Promocionada: " + actividad.isPromocionada() +
                            ", Asistido: " + actividad.isAsistido() +
                            ", ImagenRuta: " + actividad.getImagenRuta());
                    if (!actividad.isPasada()) {
                        tempItemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_ACTIVIDAD, actividad, null, null));
                    }
                }

                // Fetch past activities for the user
                List<Actividad> actividadesPasadas = managerDb.obtenerActividadesPasadasPorUsuario(userId);
                Log.d("PrincipalFragment", "Total actividades pasadas recuperadas para userId " + userId + ": " + actividadesPasadas.size());
                for (Actividad actividad : actividadesPasadas) {
                    Log.d("PrincipalFragment", "Pasada - ID: " + actividad.getId() +
                            ", Título: " + actividad.getTitulo() +
                            ", Fecha: " + actividad.getFecha() +
                            ", idCreador: " + actividad.getIdCreador() +
                            ", isPasada: " + actividad.isPasada() +
                            ", Estado: " + actividad.getEstado());
                }

                // Add past activities section if there are any
                if (!actividadesPasadas.isEmpty()) {
                    tempItemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_TITULO, null, getString(R.string.actividades_pasadas).toUpperCase(Locale.getDefault()), null));
                    tempItemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_PASADAS, null, null, actividadesPasadas));
                } else {
                    Log.w("PrincipalFragment", "No se encontraron actividades pasadas para mostrar.");
                }
            } else {
                Log.w("PrincipalFragment", "User ID is invalid: " + userId);
            }

            requireActivity().runOnUiThread(() -> {
                itemList.clear();
                itemList.addAll(tempItemList);
                Log.d("PrincipalFragment", "Total de ítems cargados en el RecyclerView: " + itemList.size());
                actividadAdapter.notifyDataSetChanged();
                actualizarVisibilidad();
                if (!itemList.isEmpty()) {
                    recyclerActividades.scrollToPosition(0); // Scroll to top to see new activities
                    adjustScrollBehavior(); // Ajustar el comportamiento de scroll según el número de ítems
                }
            });
        });
    }

    private void adjustScrollBehavior() {
        if (recyclerActividades != null && recyclerActividades.getAdapter() != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;
            int navigationBarHeight = getNavigationBarHeight();
            int usableHeight = screenHeight - navigationBarHeight;

            // Estimar la altura de los ítems
            float dpToPx = getResources().getDisplayMetrics().density;
            int actividadItemHeight = (int) (150 * dpToPx) + 32; // 150dp por actividad + 16dp de margen superior + 16dp de margen inferior
            int tituloSectionHeight = (int) (40 * dpToPx) + 16; // 40dp por título de sección + 8dp de margen superior + 8dp de margen inferior

            // Calcular la altura total del contenido
            int totalHeight = 0;
            int actividadCount = 0;
            int pasadasCount = 0;
            boolean hasPasadasSection = false;

            for (ActividadAdapter.Item item : itemList) {
                if (item.getType() == ActividadAdapter.Item.TYPE_ACTIVIDAD) {
                    actividadCount++;
                } else if (item.getType() == ActividadAdapter.Item.TYPE_TITULO) {
                    totalHeight += tituloSectionHeight;
                    hasPasadasSection = true;
                } else if (item.getType() == ActividadAdapter.Item.TYPE_PASADAS) {
                    List<Actividad> actividadesPasadas = item.getActividadesPasadas();
                    if (actividadesPasadas != null) {
                        pasadasCount = actividadesPasadas.size();
                    }
                }
            }

            totalHeight += actividadCount * actividadItemHeight; // Altura de actividades no pasadas
            totalHeight += pasadasCount * actividadItemHeight; // Altura de actividades pasadas

            // Si hay una sección de actividades pasadas, ajustar el comportamiento
            if (totalHeight <= usableHeight && actividadCount <= 1 && pasadasCount <= 1 && !hasPasadasSection) {
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