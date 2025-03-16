package com.example.sentiseguro.utils;
import com.example.sentiseguro.R;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class SalirApp extends DialogFragment {

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(getString(R.string.exit_confirmation_title))
                .setMessage(getString(R.string.exit_confirmation_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    if (getActivity() != null) {
                        getActivity().finishAffinity(); // Cierra todas las actividades de la app
                    }
                })
                .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
