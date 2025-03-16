package com.example.sentiseguro.services;

import com.example.sentiseguro.R;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificacionController {

    private static final String CHANNEL_ID = "UbicacionAlerta";
    private static final int REQUEST_CODE_NOTIFICATION = 11;
    // Puedes usar un ID fijo para la notificación persistente
    private static final int NOTIFICATION_PERSISTENTE_ID = 1001;

    /**
     * Solicita permisos de notificación en Android 13+.
     *
     * @param activity la actividad actual.
     */
    public static void solicitarPermisos(@NonNull android.app.Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(activity, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_CODE_NOTIFICATION);
        }
    }

    /**
     * Crea el canal de notificación si no existe (para Android 8+).
     *
     * @param context el contexto de la aplicación.
     */
    private static void crearCanalNotificacion(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                String channelName = context.getString(R.string.notification_channel_name);
                String channelDescription = context.getString(R.string.notification_channel_description);
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(channelDescription);
                manager.createNotificationChannel(channel);
                Log.d(context.getString(R.string.log_notification_tag), 
                      context.getString(R.string.notification_channel_created, channelName));
            }
        }
    }

    /**
     * Muestra una notificación "normal" (no persistente).
     *
     * @param context        Contexto de la aplicación.
     * @param titulo         Título de la notificación.
     * @param mensaje        Contenido del mensaje.
     * @param notificationId ID único para la notificación.
     */
    public static void mostrarNotificacion(@NonNull Context context, @NonNull String titulo, @NonNull String mensaje, int notificationId) {
        Log.d(context.getString(R.string.log_notification_tag), context.getString(R.string.notification_showing));
        crearCanalNotificacion(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
            Log.d(context.getString(R.string.log_notification_tag), 
                  context.getString(R.string.notification_sent, notificationId));
        } else {
            Log.e(context.getString(R.string.log_notification_tag), context.getString(R.string.notification_manager_null));
        }
    }

    /**
     * Muestra una notificación persistente (ongoing) que no se puede descartar, ideal para indicar que se está dentro de un rango.
     *
     * @param context Contexto de la aplicación.
     * @param titulo  Título de la notificación.
     * @param mensaje Contenido del mensaje.
     */
    public static void mostrarNotificacionPersistente(@NonNull Context context, @NonNull String titulo, @NonNull String mensaje) {
        Log.d(context.getString(R.string.log_notification_tag), context.getString(R.string.persistent_notification_showing));
        crearCanalNotificacion(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)     // La notificación es persistente
                .setAutoCancel(false); // No se cancela al tocarla

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_PERSISTENTE_ID, builder.build());
            Log.d(context.getString(R.string.log_notification_tag), 
                  context.getString(R.string.persistent_notification_shown, NOTIFICATION_PERSISTENTE_ID));
        } else {
            Log.e(context.getString(R.string.log_notification_tag), context.getString(R.string.notification_manager_null));
        }
    }

    /**
     * Cancela la notificación persistente.
     *
     * @param context Contexto de la aplicación.
     */
    public static void cancelarNotificacionPersistente(@NonNull Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(NOTIFICATION_PERSISTENTE_ID);
            Log.d(context.getString(R.string.log_notification_tag), context.getString(R.string.persistent_notification_cancelled));
        }
    }
    
    /**
     * Muestra una notificación para indicar que se ha creado una nueva zona segura.
     *
     * @param context         Contexto de la aplicación.
     * @param nombreUbicacion Nombre de la nueva ubicación.
     */
    public static void notificarZonaSeguraCreada(@NonNull Context context, @NonNull String nombreUbicacion) {
        String titulo = context.getString(R.string.safe_zone_created_title);
        String mensaje = context.getString(R.string.safe_zone_created_message, nombreUbicacion);
        mostrarNotificacion(context, titulo, mensaje, (int) System.currentTimeMillis());
    }
}
