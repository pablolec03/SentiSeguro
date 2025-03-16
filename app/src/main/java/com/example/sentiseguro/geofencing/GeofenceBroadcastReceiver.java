package com.example.sentiseguro.geofencing;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.sentiseguro.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceReceiver";
    private static final String CHANNEL_ID = "ubicacion_alertas_channel";
    // ID fijo para la notificación persistente
    private static final int PERSISTENT_NOTIFICATION_ID = 2000;

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event == null || event.hasError()) {
            Log.e(TAG, "Error en el Geofence Event: " + (event != null ? event.getErrorCode() : "Evento nulo"));
            return;
        }

        int transitionType = event.getGeofenceTransition();
        List<Geofence> geofences = event.getTriggeringGeofences();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        for (Geofence geofence : geofences) {
            String mensaje = obtenerMensajeTransicion(context, transitionType, geofence.getRequestId());
            if (mensaje != null && notificationManager != null) {
                if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    // Mostrar notificación persistente (ongoing)
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_map)
                            .setContentTitle(context.getString(R.string.notification_alert_title))
                            .setContentText(mensaje)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setOngoing(true)      // Hace la notificación persistente
                            .setAutoCancel(false); // No se cancela al tocarla
                    notificationManager.notify(PERSISTENT_NOTIFICATION_ID, builder.build());
                    Log.d(TAG, "Notificación persistente mostrada: " + mensaje);
                } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    // Cancelar la notificación persistente al salir
                    notificationManager.cancel(PERSISTENT_NOTIFICATION_ID);
                    // Mostrar una notificación breve indicando salida
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_map)
                            .setContentTitle(context.getString(R.string.notification_alert_title))
                            .setContentText(mensaje)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true);
                    notificationManager.notify((int) System.currentTimeMillis(), builder.build());
                    Log.d(TAG, "Notificación de salida mostrada: " + mensaje);
                }
            }
        }
    }

    /**
     * Obtiene el mensaje correspondiente según el tipo de transición.
     *
     * @param context        Contexto de la aplicación.
     * @param transitionType Tipo de transición (entrada o salida).
     * @param requestId      ID de la geocerca.
     * @return Mensaje descriptivo de la transición.
     */
    private String obtenerMensajeTransicion(Context context, int transitionType, String requestId) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return String.format(context.getString(R.string.entered_safe_zone), requestId);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return String.format(context.getString(R.string.exited_safe_zone), requestId);
            default:
                Log.e(TAG, String.format(context.getString(R.string.unknown_transition), transitionType));
                return null;
        }
    }
}
