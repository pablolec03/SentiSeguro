package com.example.sentiseguro.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sentiseguro.R;
import com.example.sentiseguro.baseDatos.Ubicacion;
import com.example.sentiseguro.geofencing.GeofenceHelper;
import com.example.sentiseguro.services.NotificacionController;
import com.example.sentiseguro.ui.UbicacionAdapter;
import com.example.sentiseguro.utils.EliminarUbicacionDialog;
import com.example.sentiseguro.utils.SalirApp;
import com.example.sentiseguro.utils.SettingsActivity;
import com.example.sentiseguro.viewmodel.UbicacionViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements EliminarUbicacionDialog.OnEliminarListener {

    private RecyclerView recyclerView;
    private UbicacionAdapter adapter;
    private UbicacionViewModel ubicacionViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences sharedPreferences;
    private GeofenceHelper geofenceHelper;

    // Variables para Navigation Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private static final int EDITAR_UBICACION_REQUEST = 1;
    private static final int AÑADIR_UBICACION_REQUEST = 2;
    private static final int PERMISO_UBICACION_REQUEST = 100;

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("PreferenciasApp", MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es");
        Locale newLocale = new Locale(idioma);
        Locale.setDefault(newLocale);

        Configuration configuration = newBase.getResources().getConfiguration();
        configuration.setLocale(newLocale);
        Context context = newBase.createConfigurationContext(configuration);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Inicializar SharedPreferences y configurar el idioma antes de setContentView
        sharedPreferences = getSharedPreferences("PreferenciasApp", MODE_PRIVATE);
        configurarIdioma();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configurarToolbar();
        inicializarComponentes();
        configurarNavigationDrawer();
        observarUbicaciones();
    }

    /**
     * Configura el idioma de la aplicación basado en la preferencia guardada.
     */
    private void configurarIdioma() {
        String idioma = sharedPreferences.getString("idioma", "es");
        Locale locale = new Locale(idioma);
        Locale.setDefault(locale);
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    /**
     * Cambia el idioma de la aplicación y lo guarda en las preferencias.
     *
     * @param lang Código del idioma ("en" para inglés, "es" para español).
     */
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        sharedPreferences.edit().putString("idioma", lang).apply();
        recreate();
    }

    /**
     * Configura la Toolbar.
     */
    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Se obtiene el título desde el recurso
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
    }

    /**
     * Inicializa los componentes principales de la actividad.
     */
    private void inicializarComponentes() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar ViewModel
        ubicacionViewModel = new ViewModelProvider(this).get(UbicacionViewModel.class);

        // Inicializar adaptador con los eventos de edición y borrado
        adapter = new UbicacionAdapter(new UbicacionAdapter.OnItemClickListener() {
            @Override
            public void onEditar(Ubicacion ubicacion) {
                Intent intent = new Intent(MainActivity.this, UbicacionFormActivity.class);
                intent.putExtra("ubicacion_id", ubicacion.getId());
                startActivityForResult(intent, EDITAR_UBICACION_REQUEST);
            }

            @Override
            public void onBorrar(Ubicacion ubicacion) {
                // Mostrar diálogo de confirmación antes de eliminar
                new EliminarUbicacionDialog(ubicacion).show(getSupportFragmentManager(), "EliminarUbicacionDialog");
            }
        });
        recyclerView.setAdapter(adapter);

        // Inicializar servicios de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configurar notificaciones y modo oscuro
        configurarNotificaciones();
        configurarModoOscuro();
    }

    /**
     * Configura el Navigation Drawer.
     */
    private void configurarNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(MainActivity.this, getString(R.string.nav_home), Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_mapa) {
                startActivity(new Intent(MainActivity.this, MapaActivity.class));
            } else if (id == R.id.nav_add_location) {
                startActivity(new Intent(MainActivity.this, UbicacionFormActivity.class));
            } else if (id == R.id.nav_change_language) {
                alternarIdioma();
            } else if (id == R.id.nav_dark_mode) {
                alternarModo();
            } else if (id == R.id.nav_config) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_exit) {
                new SalirApp().show(getSupportFragmentManager(), "SalirDialogo");
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    /**
     * Observa cambios en la base de datos y actualiza la UI.
     */
    private void observarUbicaciones() {
        ubicacionViewModel.getUbicaciones().observe(this, ubicaciones -> {
            adapter.updateList(ubicaciones);
            if (geofenceHelper == null) {
                geofenceHelper = new GeofenceHelper(this);
            }
            agregarGeofences(ubicaciones);
        });
    }

    /**
     * Configura las notificaciones de la aplicación.
     */
    private void configurarNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "ubicacion_alertas_channel",
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        NotificacionController.solicitarPermisos(this);
    }

    /**
     * Configura el modo oscuro según las preferencias guardadas.
     */
    private void configurarModoOscuro() {
        boolean modoOscuro = sharedPreferences.getBoolean("modo_oscuro", false);
        AppCompatDelegate.setDefaultNightMode(modoOscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    /**
     * Agrega geofences para todas las ubicaciones almacenadas en la base de datos.
     */
    private void agregarGeofences(List<Ubicacion> ubicaciones) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISO_UBICACION_REQUEST
            );
            return;
        }
        for (Ubicacion ubicacion : ubicaciones) {
            geofenceHelper.agregarGeofence(ubicacion);
        }
    }

    /**
     * Crea el menú de opciones en la Toolbar, incluyendo búsqueda, salir, exportar y opciones adicionales.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        boolean showAgregar = sharedPreferences.getBoolean("toolbar_agregar", false);
        boolean showMapa = sharedPreferences.getBoolean("toolbar_mapa", false);
        boolean showTema = sharedPreferences.getBoolean("toolbar_tema", false);
        boolean showLanguage = sharedPreferences.getBoolean("toolbar_language", false);

        if (showAgregar) {
            menu.add(Menu.NONE, R.id.action_agregar, Menu.NONE, getString(R.string.add_location))
                .setIcon(R.drawable.ic_anadir)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        if (showMapa) {
            menu.add(Menu.NONE, R.id.action_mapa, Menu.NONE, getString(R.string.view_map))
                .setIcon(R.drawable.ic_mapa)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        if (showTema) {
            menu.add(Menu.NONE, R.id.action_tema, Menu.NONE, getString(R.string.dark_mode))
                .setIcon(R.drawable.ic_oscuro)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        if (showLanguage) {
            menu.add(Menu.NONE, R.id.action_language, Menu.NONE, getString(R.string.change_language))
                .setIcon(R.drawable.ic_language)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        // Nuevo ítem para exportar ubicaciones
        menu.add(Menu.NONE, R.id.action_export, Menu.NONE, getString(R.string.export_locations_menu))
                .setIcon(R.drawable.ic_descarga)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    /**
     * Maneja la selección de opciones en la Toolbar.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.action_salir) {
            new SalirApp().show(getSupportFragmentManager(), "SalirDialogo");
            return true;
        } else if (id == R.id.action_agregar) {
            startActivityForResult(new Intent(this, UbicacionFormActivity.class), AÑADIR_UBICACION_REQUEST);
            return true;
        } else if (id == R.id.action_mapa) {
            startActivity(new Intent(this, MapaActivity.class));
            return true;
        } else if (id == R.id.action_tema) {
            alternarModo();
            return true;
        } else if (id == R.id.action_language) {
            alternarIdioma();
            return true;
        } else if (id == R.id.action_export) {
            exportLocationsToTxt();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Al reanudar la actividad se fuerza la recreación del menú para reflejar cambios.
     */
    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    /**
     * Alterna el modo oscuro y lo guarda en SharedPreferences.
     */
    private void alternarModo() {
        boolean modoOscuro = !sharedPreferences.getBoolean("modo_oscuro", false);
        sharedPreferences.edit().putBoolean("modo_oscuro", modoOscuro).apply();
        AppCompatDelegate.setDefaultNightMode(modoOscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    /**
     * Alterna el idioma entre español e inglés y lo guarda en SharedPreferences.
     */
    private void alternarIdioma() {
        String idiomaActual = sharedPreferences.getString("idioma", "es");
        String nuevoIdioma = idiomaActual.equals("es") ? "en" : "es";
        setLocale(nuevoIdioma);
    }

    // Callback del diálogo de confirmación de eliminación
    @Override
    public void onConfirmarEliminar(Ubicacion ubicacion) {
        ubicacionViewModel.eliminarUbicacion(ubicacion);
        Toast.makeText(this, getString(R.string.location_deleted), Toast.LENGTH_SHORT).show();
    }

    /**
     * Método para exportar todas las ubicaciones a un archivo TXT.
     * Se muestra un diálogo de confirmación; si el usuario acepta, se genera el archivo en segundo plano,
     * se notifica al usuario y se lanza un Intent para compartirlo.
     */
    public void exportLocationsToTxt() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.export_locations_title))
                .setMessage(getString(R.string.export_locations_message))
                .setPositiveButton(getString(R.string.export_locations_positive), (dialog, which) -> {
                    AsyncTask.execute(() -> {
                        // Obtener las ubicaciones de forma síncrona usando obtenerTodasSync()
                        List<Ubicacion> locations = com.example.sentiseguro.baseDatos.UbicacionDatabase.getInstance(this)
                                .ubicacionDao().obtenerTodasSync();
                        
                        StringBuilder sb = new StringBuilder();
                        for (Ubicacion ubicacion : locations) {
                            sb.append(getString(R.string.export_field_nombre)).append(": ").append(ubicacion.getNombre()).append("\n");
                            sb.append(getString(R.string.export_field_direccion)).append(": ").append(ubicacion.getDireccion()).append("\n");
                            sb.append(getString(R.string.export_field_latitud)).append(": ").append(ubicacion.getLatitud()).append("\n");
                            sb.append(getString(R.string.export_field_longitud)).append(": ").append(ubicacion.getLongitud()).append("\n");
                            sb.append(getString(R.string.export_field_rango)).append(": ").append(ubicacion.getRango())
                              .append(" ").append(getString(R.string.export_unit_m)).append("\n");
                            sb.append(getString(R.string.export_field_contacto)).append(": ").append(ubicacion.getContacto()).append("\n");
                            sb.append("------------------------------\n");
                        }
                        
                        try {
                            File file = new File(getExternalFilesDir(null), "ubicaciones.txt");
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(sb.toString().getBytes());
                            fos.close();
                            
                            Uri contentUri = FileProvider.getUriForFile(this,
                                    "com.example.sentiseguro.fileprovider", file);
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            
                            runOnUiThread(() -> {
                                Toast.makeText(this, getString(R.string.export_locations_success), Toast.LENGTH_SHORT).show();
                                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_locations)));
                            });
                            
                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> Toast.makeText(this, getString(R.string.export_locations_error), Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(getString(R.string.export_locations_negative), null)
                .show();
    }
}    