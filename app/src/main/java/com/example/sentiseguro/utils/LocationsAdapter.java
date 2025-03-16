package com.example.sentiseguro.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sentiseguro.R;
import com.example.sentiseguro.baseDatos.Ubicacion;

import java.util.List;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.LocationViewHolder> {

    private List<Ubicacion> locations;
    private OnLocationActionListener listener;

    public interface OnLocationActionListener {
        void onEdit(Ubicacion ubicacion);
        void onDelete(Ubicacion ubicacion);
        void onCall(Ubicacion ubicacion);
    }

    public LocationsAdapter(List<Ubicacion> locations, OnLocationActionListener listener) {
        this.locations = locations;
        this.listener = listener;
    }

    public void setLocations(List<Ubicacion> locations) {
        this.locations = locations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout item_ubicacion.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ubicacion, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Ubicacion ubicacion = locations.get(position);
        holder.tvNombre.setText(ubicacion.getNombre());
        holder.tvDireccion.setText(ubicacion.getDireccion());
        // Mostrar el rango y agregar la unidad (por ejemplo, "m")
        holder.tvRango.setText(String.valueOf(ubicacion.getRango()) + " m");

        // Asignar los listeners a cada botón
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(ubicacion);
            }
        });
        holder.btnBorrar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(ubicacion);
            }
        });
        holder.btnLlamar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCall(ubicacion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locations != null ? locations.size() : 0;
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDireccion, tvRango;
        ImageButton btnEditar, btnBorrar, btnLlamar;

        LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre);
            tvDireccion = itemView.findViewById(R.id.tv_direccion);
            tvRango = itemView.findViewById(R.id.tv_rango);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnBorrar = itemView.findViewById(R.id.btn_borrar);
            btnLlamar = itemView.findViewById(R.id.btn_llamar);
        }
    }
}
