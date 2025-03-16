package com.example.sentiseguro.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sentiseguro.R;
import com.example.sentiseguro.baseDatos.Ubicacion;
import com.example.sentiseguro.viewmodel.UbicacionViewModel;

import java.util.ArrayList;

public class LocationsListFragment extends Fragment {

    private RecyclerView recyclerView;
    private LocationsAdapter adapter;
    private UbicacionViewModel ubicacionViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_locations_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerView = view.findViewById(R.id.recycler_locations);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new LocationsAdapter(new ArrayList<>(), new LocationsAdapter.OnLocationActionListener() {
            @Override
            public void onEdit(Ubicacion ubicacion) {
                Toast.makeText(getContext(), "Editar: " + ubicacion.getNombre(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onDelete(Ubicacion ubicacion) {
                Toast.makeText(getContext(), "Eliminar: " + ubicacion.getNombre(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCall(Ubicacion ubicacion) {
                Toast.makeText(getContext(), "Llamar: " + ubicacion.getContacto(), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        ubicacionViewModel = new ViewModelProvider(requireActivity()).get(UbicacionViewModel.class);
        ubicacionViewModel.getUbicaciones().observe(getViewLifecycleOwner(), ubicaciones -> {
            adapter.setLocations(ubicaciones);
        });
    }
}
