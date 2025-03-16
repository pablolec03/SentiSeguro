package com.example.sentiseguro.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.sentiseguro.R;
import com.example.sentiseguro.baseDatos.Ubicacion;

public class EliminarUbicacionDialog extends DialogFragment {

    public interface OnEliminarListener {
        void onConfirmarEliminar(Ubicacion ubicacion);
    }

    private OnEliminarListener listener;
    private Ubicacion ubicacion;

    public EliminarUbicacionDialog(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verifica si la Activity o el Fragment que muestra este diálogo implementa la interfaz
        if (context instanceof OnEliminarListener) {
            listener = (OnEliminarListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " debe implementar OnEliminarListener para manejar la confirmación.");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirm_delete_title))
                .setMessage(getString(R.string.confirm_delete_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    if (listener != null) {
                        listener.onConfirmarEliminar(ubicacion);
                    }
                })
                .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss())
                .create();
    }
}
