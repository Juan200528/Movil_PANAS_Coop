package com.juan.movil_panas_coop.ui.actividades;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.ui.actividad_promocionada.PromocionadasFragment;
import com.juan.movil_panas_coop.ui.busca_filtrar_actividades.BuscarFragment;
import com.juan.movil_panas_coop.ui.lista_actividades.ListaFragment;
import com.juan.movil_panas_coop.ui.recordatorio.RecordatorioFragment;

public class FragmentActPanel extends Fragment {

    private CardView cardBuscarFiltrar, cardListaActividades, cardPromocionadas, cardRecordatorio;

    public FragmentActPanel() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_actipanel, container, false);

        cardBuscarFiltrar = view.findViewById(R.id.card_buscar_filtrar);
        cardListaActividades = view.findViewById(R.id.card_lista_actividades);
        cardPromocionadas = view.findViewById(R.id.card_actividades_promocionadas);
        cardRecordatorio = view.findViewById(R.id.card_recordatorio);

        cardBuscarFiltrar.setOnClickListener(v -> abrirFragmento(new BuscarFragment())); //Poner fragment de gestionar
        cardListaActividades.setOnClickListener(v -> abrirFragmento(new BuscarFragment()));
        cardPromocionadas.setOnClickListener(v -> abrirFragmento(new PromocionadasFragment()));
        cardRecordatorio.setOnClickListener(v -> abrirFragmento(new RecordatorioFragment()));

        // Aplicar efecto de "presión" a los LinearLayout simulando botones
        aplicarEfectoPresion(view.findViewById(R.id.miBotton));
        aplicarEfectoPresion(view.findViewById(R.id.miBotton2));
        aplicarEfectoPresion(view.findViewById(R.id.miBotton3));
        aplicarEfectoPresion(view.findViewById(R.id.miBotton4));

        return view;
    }

    private void abrirFragmento(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Aplica animación al presionar el "botón"
    private void aplicarEfectoPresion(View boton) {
        if (boton != null) {
            boton.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start(); // Efecto "atracción"
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start(); // Volver a tamaño normal
                        break;
                }
                return false; // Permitir que el evento de click aún se propague al CardView
            });
        }
    }
}
