package com.juan.movil_panas_coop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.juan.movil_panas_coop.ui.actividades.ActividadesFragment;
import com.juan.movil_panas_coop.ui.comunidades.ComunidadesFragment;
import com.juan.movil_panas_coop.ui.perfil.PerfilFragment;
import com.juan.movil_panas_coop.ui.principal.PrincipalFragment;
import com.juan.movil_panas_coop.R;

public class MenuActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabCreateActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabCreateActivity = findViewById(R.id.fabCreateActivity);

        // Depuración: Verificar si el FAB se inicializó
        if (fabCreateActivity != null) {
            Log.d("MenuActivity", "FAB inicializado correctamente");
            fabCreateActivity.setVisibility(View.VISIBLE); // Mostrar FAB inmediatamente
        } else {
            Log.e("MenuActivity", "FAB es null, no se encontró en el layout");
        }

        if (savedInstanceState == null) {
            loadFragment(new PrincipalFragment());
        }

        bottomNavigationView.setSelectedItemId(R.id.nav_inicio);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_inicio) {
                fragment = new PrincipalFragment();
                fabCreateActivity.setVisibility(View.VISIBLE); // Mostrar FAB
            } else if (itemId == R.id.nav_actividades) {
                fragment = new ActividadesFragment();
                fabCreateActivity.setVisibility(View.GONE); // Ocultar FAB
            } else if (itemId == R.id.nav_comunidades) {
                fragment = new ComunidadesFragment();
                fabCreateActivity.setVisibility(View.GONE); // Ocultar FAB
            } else if (itemId == R.id.nav_perfil) {
                fragment = new PerfilFragment();
                fabCreateActivity.setVisibility(View.GONE); // Ocultar FAB
            }

            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        });

        fabCreateActivity.setOnClickListener(v -> {
            Log.d("MenuActivity", "FAB clicked");
            Intent intent = new Intent(this, CrearActividad.class);
            startActivityForResult(intent, 1);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Recargar actividades si es necesario
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof PrincipalFragment) {
                ((PrincipalFragment) currentFragment).cargarActividades();
            }
        }
    }
}