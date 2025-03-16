package com.example.sentiseguro.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import com.example.sentiseguro.R;
import com.example.sentiseguro.baseDatos.Ubicacion;
import com.example.sentiseguro.baseDatos.UbicacionDatabase;
import com.example.sentiseguro.services.NotificacionController;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class UbicacionFormActivity extends AppCompatActivity {

    private static final int PICK_CONTACT_REQUEST = 1;
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private EditText etNombre, etDireccion, etRango, etContacto;
    private Button btnGuardar, btnSeleccionarContacto, btnUbicacionActual;
    private FusedLocationProviderClient fusedLocationClient;
    private UbicacionDatabase db;
    private Ubicacion ubicacion;
    private boolean esEdicion = false;

    // Variables para almacenar las coordenadas obtenidas o pasadas
    private double latitud = 0.0, longitud = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_ubicacion);

        etNombre = findViewById(R.id.et_nombre);
        etDireccion = findViewById(R.id.et_direccion);
        etRango = findViewById(R.id.et_rango);
        etContacto = findViewById(R.id.et_contacto);
        btnGuardar = findViewById(R.id.btn_guardar);
        btnSeleccionarContacto = findViewById(R.id.btn_seleccionar_contacto);
        btnUbicacionActual = findViewById(R.id.btn_ubicacion_actual);

        db = UbicacionDatabase.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Verifica si se está editando una ubicación existente
        int ubicacionId = getIntent().getIntExtra("ubicacion_id", -1);
        if (ubicacionId != -1) {
            esEdicion = true;
            cargarUbicacion(ubicacionId);
        } else if (getIntent().hasExtra("latitud") && getIntent().hasExtra("longitud")) {
            // Si se han pasado coordenadas desde el mapa, prellenar con los datos del punto
            latitud = getIntent().getDoubleExtra("latitud", 0.0);
            longitud = getIntent().getDoubleExtra("longitud", 0.0);
            etDireccion.setText(String.format(getString(R.string.location_coordinates_format), latitud, longitud));
        }

        btnSeleccionarContacto.setOnClickListener(v -> seleccionarContacto());
        btnUbicacionActual.setOnClickListener(v -> obtenerUbicacionActual());
        btnGuardar.setOnClickListener(v -> guardarUbicacion());
    }

    /**
     * Carga la ubicación para edición utilizando LiveData.
     */
    private void cargarUbicacion(int id) {
        db.ubicacionDao().obtenerPorId(id).observe(this, new Observer<Ubicacion>() {
            @Override
            public void onChanged(Ubicacion ubicacionFromDb) {
                if (ubicacionFromDb != null) {
                    ubicacion = ubicacionFromDb;
                    etNombre.setText(ubicacion.getNombre());
                    etDireccion.setText(ubicacion.getDireccion());
                    etRango.setText(String.valueOf(ubicacion.getRango()));
                    etContacto.setText(ubicacion.getContacto());
                    // Asignar latitud y longitud para su posible actualización
                    latitud = ubicacion.getLatitud();
                    longitud = ubicacion.getLongitud();
                }
            }
        });
    }

    /**
     * Obtiene la ubicación actual usando FusedLocationProviderClient.
     */
    private void obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitud = location.getLatitude();
                longitud = location.getLongitude();
                etDireccion.setText(String.format(getString(R.string.location_coordinates_format), latitud, longitud));
            } else {
                Toast.makeText(this, getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Lanza la actividad para seleccionar un contacto.
     */
    private void seleccionarContacto() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                try (android.database.Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String phoneNumber = cursor.getString(numberIndex);
                        etContacto.setText(phoneNumber);
                    }
                }
            }
        }
    }

    /**
     * Guarda o actualiza la ubicación en la base de datos.
     */
    private void guardarUbicacion() {
        String nombre = etNombre.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String rangoStr = etRango.getText().toString().trim();
        String contacto = etContacto.getText().toString().trim();

        if (nombre.isEmpty() || direccion.isEmpty() || rangoStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.complete_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        int rango;
        try {
            rango = Integer.parseInt(rangoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.range_must_be_number), Toast.LENGTH_SHORT).show();
            return;
        }

        if (esEdicion && ubicacion != null) {
            // Actualizar la ubicación existente
            ubicacion.setNombre(nombre);
            ubicacion.setDireccion(direccion);
            ubicacion.setRango(rango);
            ubicacion.setContacto(contacto);
            ubicacion.setLatitud(latitud);
            ubicacion.setLongitud(longitud);

            new Thread(() -> {
                db.ubicacionDao().actualizar(ubicacion);
                runOnUiThread(() -> {
                    Toast.makeText(UbicacionFormActivity.this, getString(R.string.location_updated), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK, new Intent().putExtra("ubicacion_guardada", true));
                    finish();
                });
            }).start();
        } else {
            // Crear una nueva ubicación utilizando los datos actuales, incluida la ubicación
            Ubicacion nuevaUbicacion = new Ubicacion(nombre, direccion, latitud, longitud, rango, contacto);
            new Thread(() -> {
                db.ubicacionDao().insertar(nuevaUbicacion);
                runOnUiThread(() -> {
                    // Mostrar notificación al crear una nueva zona segura
                    NotificacionController.mostrarNotificacion(
                            UbicacionFormActivity.this,
                            getString(R.string.safe_zone_created_title),
                            getString(R.string.safe_zone_created_message, nombre),
                            (int) System.currentTimeMillis()
                    );
                    Toast.makeText(UbicacionFormActivity.this, getString(R.string.location_saved), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK, new Intent().putExtra("ubicacion_guardada", true));
                    finish();
                });
            }).start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual();
            } else {
                Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
