package com.juan.movil_panas_coop;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class FragmentActPanel extends Fragment {

    private CardView cardBuscarFiltrar, cardListaActividades, cardPromocionadas, cardRecordatorio;

    public FragmentActPanel() {
        // Constructor vacío requerido
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_actipanel, container, false);

        // Referenciar las tarjetas
        cardBuscarFiltrar = view.findViewById(R.id.card_buscar_filtrar);
        cardListaActividades = view.findViewById(R.id.card_lista_actividades);
        cardPromocionadas = view.findViewById(R.id.card_actividades_promocionadas);
        cardRecordatorio = view.findViewById(R.id.card_recordatorio);

        // Configurar eventos de clic
        cardBuscarFiltrar.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Buscar y filtrar actividades", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(getActivity(), BuscarFiltrarActivity.class));
        });

        cardListaActividades.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Lista de actividades", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(getActivity(), ListaActividadesActivity.class));
        });

        cardPromocionadas.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Actividades promocionadas", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(getActivity(), ActividadesPromocionadasActivity.class));
        });

        cardRecordatorio.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Recordatorios", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(getActivity(), RecordatoriosActivity.class));
        });

        return view;
    }
}
