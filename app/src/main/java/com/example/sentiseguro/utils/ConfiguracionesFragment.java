package com.example.sentiseguro.utils;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.example.sentiseguro.R;

public class ConfiguracionesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Configura que se usen las mismas preferencias que en MainActivity
        getPreferenceManager().setSharedPreferencesName("PreferenciasApp");
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
