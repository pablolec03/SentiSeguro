package com.example.sentiseguro.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.sentiseguro.R;
import com.example.sentiseguro.baseDatos.Ubicacion;
import com.example.sentiseguro.geofencing.GeofenceHelper;
import com.example.sentiseguro.viewmodel.UbicacionViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private GeofenceHelper geofenceHelper;
    private UbicacionViewModel ubicacionViewModel;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FloatingActionButton btnMiUbicacion;
    private MaterialButton btnAgregarUbicacion; // Botón para agregar ubicación

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        inicializarComponentes();
        configurarBotonMiUbicacion();  // Configura el botón flotante para centrar el mapa
        configurarBotonAgregarUbicacion(); // Configura el botón para agregar ubicación manualmente
    }

    /**
     * Inicializa el ViewModel, servicios de ubicación y carga el fragmento del mapa de forma dinámica.
     */
    private void inicializarComponentes() {
        // Inicializar ViewModel
        ubicacionViewModel = new ViewModelProvider(this).get(UbicacionViewModel.class);

        // Inicializar servicios de ubicación y geofencing
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofenceHelper = new GeofenceHelper(this);

        // Cargar el SupportMapFragment de forma dinámica en el contenedor "map_container"
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();
        }
        mapFragment.getMapAsync(this);
    }

    /**
     * Configura el FloatingActionButton para centrar el mapa en la ubicación actual.
     */
    private void configurarBotonMiUbicacion() {
        btnMiUbicacion = findViewById(R.id.btn_mi_ubicacion);
        btnMiUbicacion.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null && mMap != null) {
                    LatLng ubicacionActual = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15));
                } else {
                    Toast.makeText(MapaActivity.this, getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Configura el botón para agregar ubicación manualmente.
     */
    private void configurarBotonAgregarUbicacion() {
        btnAgregarUbicacion = findViewById(R.id.btn_agregar_ubicacion);
        btnAgregarUbicacion.setOnClickListener(v -> {
            if (mMap != null) {
                // Usamos el centro actual de la cámara como coordenadas iniciales
                LatLng centroMapa = mMap.getCameraPosition().target;
                abrirFormularioUbicacion(centroMapa);
            } else {
                Toast.makeText(MapaActivity.this, getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método que se ejecuta cuando el mapa está listo para usarse.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Habilitar controles del mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        verificarPermisosUbicacion();

        // Observar las ubicaciones y mostrarlas en el mapa
        ubicacionViewModel.getUbicaciones().observe(this, this::mostrarUbicacionesEnMapa);

        // Listener para agregar una ubicación con Long Click
        mMap.setOnMapLongClickListener(latLng -> {
            Toast.makeText(MapaActivity.this, getString(R.string.adding_new_location), Toast.LENGTH_SHORT).show();
            abrirFormularioUbicacion(latLng);
        });

        // Si existe el contenedor del listado (solo en landscape), cargar el fragmento del listado de ubicaciones
        if (findViewById(R.id.list_container) != null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.list_container, new LocationsListFragment())
                .commit();
        }
    }

    /**
     * Verifica y solicita los permisos de ubicación si es necesario.
     */
    private void verificarPermisosUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            habilitarUbicacion();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Habilita la ubicación en el mapa y mueve la cámara a la ubicación actual.
     */
    private void habilitarUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng ubicacionActual = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15));
            }
        });
    }

    /**
     * Muestra las ubicaciones guardadas en el mapa, agregando marcadores, círculos y geofences.
     */
    private void mostrarUbicacionesEnMapa(List<Ubicacion> ubicaciones) {
        mMap.clear(); // Limpiar el mapa antes de agregar nuevas ubicaciones
        for (Ubicacion ubicacion : ubicaciones) {
            LatLng posicion = new LatLng(ubicacion.getLatitud(), ubicacion.getLongitud());

            // Añadir marcador
            mMap.addMarker(new MarkerOptions()
                    .position(posicion)
                    .title(ubicacion.getNombre()));

            // Dibujar círculo que representa el rango
            mMap.addCircle(new CircleOptions()
                    .center(posicion)
                    .radius(ubicacion.getRango())
                    .strokeWidth(2f)
                    .strokeColor(0xFF0000FF)
                    .fillColor(0x220000FF));

            // Agregar Geofence si se tienen los permisos de ubicación
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                geofenceHelper.agregarGeofence(ubicacion);
            }
        }
    }

    /**
     * Abre el formulario para añadir o editar una ubicación.
     */
    private void abrirFormularioUbicacion(LatLng latLng) {
        Intent intent = new Intent(this, UbicacionFormActivity.class);
        intent.putExtra("latitud", latLng.latitude);
        intent.putExtra("longitud", latLng.longitude);
        startActivity(intent);
    }

    /**
     * Maneja la respuesta a la solicitud de permisos de ubicación.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                habilitarUbicacion();
            } else {
                Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
