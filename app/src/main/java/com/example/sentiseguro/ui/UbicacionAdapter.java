package com.example.sentiseguro.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sentiseguro.R;
import com.example.sentiseguro.baseDatos.Ubicacion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class UbicacionAdapter extends RecyclerView.Adapter<UbicacionAdapter.ViewHolder> implements Filterable {

    private final List<Ubicacion> ubicaciones;
    private List<Ubicacion> ubicacionesOriginales;
    private final OnItemClickListener listener;

    public UbicacionAdapter(OnItemClickListener listener) {
        this.ubicaciones = new ArrayList<>();
        this.ubicacionesOriginales = new ArrayList<>();
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onEditar(Ubicacion ubicacion);
        void onBorrar(Ubicacion ubicacion);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ubicacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ubicacion ubicacion = ubicaciones.get(position);
        Context context = holder.itemView.getContext();

        // Establece el nombre tal cual viene
        holder.tvNombre.setText(ubicacion.getNombre());
        // Usa un string formateado para la dirección: "Dirección: %1$s"
        holder.tvDireccion.setText(context.getString(R.string.location_address, ubicacion.getDireccion()));
        // Usa un string formateado para el rango: "Rango: %1$d metros"
        holder.tvRango.setText(context.getString(R.string.location_range, ubicacion.getRango()));

        // Configuración de eventos para cada botón
        holder.btnEditar.setOnClickListener(v -> listener.onEditar(ubicacion));
        holder.btnBorrar.setOnClickListener(v -> listener.onBorrar(ubicacion));
        holder.btnLlamar.setOnClickListener(v -> llamarContacto(ubicacion, new WeakReference<>(context)));
    }

    @Override
    public int getItemCount() {
        return ubicaciones.size();
    }

    /**
     * Método para realizar la llamada al contacto vinculado a la ubicación.
     */
    private void llamarContacto(Ubicacion ubicacion, WeakReference<Context> contextRef) {
        Context context = contextRef.get();
        if (context == null) return;

        String numero = ubicacion.getContacto();
        if (numero != null && !numero.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numero));
            context.startActivity(intent);
        } else {
            Toast.makeText(context, context.getString(R.string.no_contact_available), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Actualiza la lista de ubicaciones y refresca la vista.
     */
    public void updateList(List<Ubicacion> nuevasUbicaciones) {
        ubicaciones.clear();
        ubicaciones.addAll(nuevasUbicaciones);
        ubicacionesOriginales = new ArrayList<>(nuevasUbicaciones);
        notifyDataSetChanged();
    }

    /**
     * Implementación del filtrado en RecyclerView.
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Ubicacion> listaFiltrada = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    listaFiltrada.addAll(ubicacionesOriginales);
                } else {
                    String filtro = constraint.toString().toLowerCase().trim();
                    for (Ubicacion ubicacion : ubicacionesOriginales) {
                        if (ubicacion.getNombre().toLowerCase().contains(filtro) ||
                                ubicacion.getDireccion().toLowerCase().contains(filtro)) {
                            listaFiltrada.add(ubicacion);
                        }
                    }
                }
                FilterResults resultados = new FilterResults();
                resultados.values = listaFiltrada;
                resultados.count = listaFiltrada.size();
                return resultados;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                ubicaciones.clear();
                if (results.values != null) {
                    ubicaciones.addAll((List<Ubicacion>) results.values);
                }
                // Se usa notifyDataSetChanged para refrescar completamente la lista
                notifyDataSetChanged();
            }
        };
    }

    /**
     * ViewHolder optimizado que mantiene referencias a los elementos de la UI.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNombre, tvDireccion, tvRango;
        final ImageButton btnEditar, btnBorrar, btnLlamar;

        public ViewHolder(@NonNull View itemView) {
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
