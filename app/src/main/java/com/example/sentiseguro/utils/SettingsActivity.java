package com.example.sentiseguro.utils;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sentiseguro.utils.ConfiguracionesFragment;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Se reemplaza el contenido de la actividad por el fragmento de preferencias
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new ConfiguracionesFragment())
                .commit();
    }
}
