package com.example.sentiseguro.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.sentiseguro.R;
import com.example.sentiseguro.baseDatos.Ubicacion;
import com.example.sentiseguro.baseDatos.UbicacionDatabase;
import com.example.sentiseguro.geofencing.GeofenceHelper;

import java.util.List;

public class NotificacionService extends Service {

    private static final String CHANNEL_ID = "NotificacionServiceChannel";
    private static final int SERVICE_NOTIFICATION_ID = 101;

    private GeofenceHelper geofenceHelper;
    private Observer<List<Ubicacion>> ubicacionesObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("NotificacionService", getString(R.string.service_started));

        geofenceHelper = new GeofenceHelper(this);
        iniciarServicioEnPrimerPlano();
        observarUbicaciones();
    }

    /**
     * Inicia el servicio en primer plano con una notificación persistente.
     */
    private void iniciarServicioEnPrimerPlano() {
        crearCanalDeNotificacion();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.service_notification_title))
                .setContentText(getString(R.string.service_notification_text))
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    /**
     * Crea el canal de notificación si no existe (para Android 8+).
     */
    private void crearCanalDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                String channelName = getString(R.string.service_channel_name);
                String channelDescription = getString(R.string.service_channel_description);
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(channelDescription);
                manager.createNotificationChannel(channel);
                Log.d("NotificacionService", getString(R.string.notification_channel_created, channelName));
            }
        }
    }

    /**
     * Observa la base de datos en busca de ubicaciones y actualiza los geofences.
     */
    private void observarUbicaciones() {
        UbicacionDatabase db = UbicacionDatabase.getInstance(this);
        LiveData<List<Ubicacion>> ubicacionesLiveData = db.ubicacionDao().obtenerTodas();

        ubicacionesObserver = new Observer<List<Ubicacion>>() {
            @Override
            public void onChanged(List<Ubicacion> ubicaciones) {
                Log.d("NotificacionService", getString(R.string.geofences_updating));
                for (Ubicacion ubicacion : ubicaciones) {
                    geofenceHelper.agregarGeofence(ubicacion);
                }
            }
        };

        ubicacionesLiveData.observeForever(ubicacionesObserver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // No es un servicio enlazado
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("NotificacionService", getString(R.string.service_running));
        return START_STICKY; // Mantiene el servicio activo si es finalizado por el sistema
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UbicacionDatabase db = UbicacionDatabase.getInstance(this);
        db.ubicacionDao().obtenerTodas().removeObserver(ubicacionesObserver);
        Log.d("NotificacionService", getString(R.string.service_stopped));
    }
}
