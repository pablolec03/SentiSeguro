package com.example.sentiseguro.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.sentiseguro.R;
import com.example.sentiseguro.baseDatos.Ubicacion;
import com.example.sentiseguro.baseDatos.UbicacionDatabase;
import com.example.sentiseguro.geofencing.GeofenceBroadcastReceiver;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, context.getString(R.string.boot_restarted));

            UbicacionDatabase db = UbicacionDatabase.getInstance(context);
            GeofencingClient geofencingClient = LocationServices.getGeofencingClient(context);

            // Recuperar ubicaciones desde la base de datos en un hilo en segundo plano
            new Thread(() -> {
                List<Ubicacion> ubicaciones = db.ubicacionDao().obtenerTodasSync();
                if (ubicaciones.isEmpty()) {
                    Log.d(TAG, context.getString(R.string.no_locations_found));
                    return;
                }

                List<Geofence> geofenceList = new ArrayList<>();
                for (Ubicacion ubicacion : ubicaciones) {
                    geofenceList.add(new Geofence.Builder()
                            .setRequestId(String.valueOf(ubicacion.getId()))
                            .setCircularRegion(ubicacion.getLatitud(), ubicacion.getLongitud(), ubicacion.getRango())
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build());
                }

                GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .addGeofences(geofenceList)
                        .build();

                Intent geoIntent = new Intent(context, GeofenceBroadcastReceiver.class);
                PendingIntent geofencePendingIntent = PendingIntent.getBroadcast(
                        context, 0, geoIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, context.getString(R.string.geofences_restored_successfully)))
                        .addOnFailureListener(e -> Log.e(TAG, context.getString(R.string.geofences_restored_error, e.getMessage())));
            }).start();
        }
    }
}
