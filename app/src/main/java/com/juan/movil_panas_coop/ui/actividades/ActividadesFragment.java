package com.juan.movil_panas_coop.ui.actividades;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ActividadesFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Aquí inflas el layout correspondiente a este fragmento
        // Por ahora, devolvemos una vista vacía o un layout placeholder
        return inflater.inflate(android.R.layout.simple_list_item_1, container, false);
    }
}