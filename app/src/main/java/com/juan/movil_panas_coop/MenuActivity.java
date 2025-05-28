package com.juan.movil_panas_coop;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MenuActivity extends AppCompatActivity {

    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu); // Asegúrate que ese sea tu layout

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("PanasCoop");

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        loadFragment(new InicioFragment()); // Fragmento por defecto

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.nav_inicio:
            /*        fragment = new InicioFragment();
                    break;*/
               case R.id.nav_actividades:
                    fragment = new FragmentActPanel();
                    break;
              /*  case R.id.nav_comunidades:
                    fragment = new ComunidadesFragment();
                    break;*/
               /* case R.id.nav_perfil:
                    fragment = new PerfilFragment();
                    break;*/
            }

            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Para inflar el ícono del perfil en el Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    // Acción al tocar el ícono de persona
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ic_person_toolbar) {
            Toast.makeText(this, "Perfil presionado", Toast.LENGTH_SHORT).show();
            // Aquí puedes abrir otra actividad o fragmento
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}