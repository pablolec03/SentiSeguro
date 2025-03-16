package com.example.sentiseguro.geofencing;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.sentiseguro.R;
import com.example.sentiseguro.baseDatos.Ubicacion;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.stream.Collectors;

public class GeofenceHelper {

    private static final String TAG = "GeofenceHelper";
    private static final long DEFAULT_EXPIRATION_DURATION = Geofence.NEVER_EXPIRE; // Geocercas permanentes

    private Context context;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    public GeofenceHelper(Context context) {
        this.context = context;
        geofencingClient = LocationServices.getGeofencingClient(context);
    }

    /**
     * Crea una nueva instancia de Geofence para la ubicación proporcionada.
     *
     * @param ubicacion La ubicación para la geocerca.
     * @return Un objeto Geofence configurado.
     */
    private Geofence crearGeofence(Ubicacion ubicacion) {
        return new Geofence.Builder()
                .setRequestId(String.valueOf(ubicacion.getId())) // ID único de la geocerca
                .setCircularRegion(ubicacion.getLatitud(), ubicacion.getLongitud(), ubicacion.getRango())
                .setExpirationDuration(DEFAULT_EXPIRATION_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    /**
     * Crea la solicitud de Geofence para agregar una lista de geocercas.
     *
     * @param geofences Lista de geocercas.
     * @return GeofencingRequest configurado.
     */
    private GeofencingRequest crearGeofencingRequest(List<Geofence> geofences) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build();
    }

    /**
     * Agrega una geocerca para una ubicación.
     *
     * @param ubicacion La ubicación para la geocerca.
     */
    public void agregarGeofence(Ubicacion ubicacion) {
        Geofence geofence = crearGeofence(ubicacion);
        GeofencingRequest geofencingRequest = crearGeofencingRequest(List.of(geofence));

        geofencingClient.addGeofences(geofencingRequest, obtenerGeofencePendingIntent())
                .addOnSuccessListener(aVoid -> Log.d(TAG, context.getString(R.string.geofence_added, ubicacion.getNombre())))
                .addOnFailureListener(e -> Log.e(TAG, context.getString(R.string.geofence_add_error, obtenerMensajeError(e))));
    }

    /**
     * Agrega múltiples geocercas a la vez.
     *
     * @param ubicaciones Lista de ubicaciones.
     */
    public void agregarMultiplesGeofences(List<Ubicacion> ubicaciones) {
        List<Geofence> geofences = ubicaciones.stream()
                .map(this::crearGeofence)
                .collect(Collectors.toList());

        GeofencingRequest geofencingRequest = crearGeofencingRequest(geofences);

        geofencingClient.addGeofences(geofencingRequest, obtenerGeofencePendingIntent())
                .addOnSuccessListener(aVoid -> Log.d(TAG, context.getString(R.string.multiple_geofences_added)))
                .addOnFailureListener(e -> Log.e(TAG, context.getString(R.string.multiple_geofences_add_error, obtenerMensajeError(e))));
    }

    /**
     * Elimina una geocerca usando su ID.
     *
     * @param ubicacion La ubicación cuyo geofence se desea eliminar.
     */
    public void eliminarGeofence(Ubicacion ubicacion) {
        geofencingClient.removeGeofences(List.of(String.valueOf(ubicacion.getId())))
                .addOnSuccessListener(aVoid -> Log.d(TAG, context.getString(R.string.geofence_removed, ubicacion.getNombre())))
                .addOnFailureListener(e -> Log.e(TAG, context.getString(R.string.geofence_remove_error, obtenerMensajeError(e))));
    }

    /**
     * Elimina todas las geocercas activas.
     */
    public void eliminarTodasLasGeofences() {
        geofencingClient.removeGeofences(obtenerGeofencePendingIntent())
                .addOnSuccessListener(aVoid -> Log.d(TAG, context.getString(R.string.all_geofences_removed)))
                .addOnFailureListener(e -> Log.e(TAG, context.getString(R.string.all_geofences_remove_error, obtenerMensajeError(e))));
    }

    /**
     * Obtiene el PendingIntent para manejar eventos de Geofencing.
     *
     * @return PendingIntent para Geofencing.
     */
    private PendingIntent obtenerGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return geofencePendingIntent;
    }

    /**
     * Convierte errores de Geofencing en mensajes legibles.
     *
     * @param e Excepción capturada.
     * @return Mensaje de error.
     */
    private String obtenerMensajeError(Exception e) {
        return e.getMessage() != null ? e.getMessage() : context.getString(R.string.unknown_geofence_error);
    }
}
