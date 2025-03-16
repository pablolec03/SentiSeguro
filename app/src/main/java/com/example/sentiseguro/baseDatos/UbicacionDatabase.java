package com.example.sentiseguro.baseDatos;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Ubicacion.class}, version = 2, exportSchema = false)
public abstract class UbicacionDatabase extends RoomDatabase {

    public abstract UbicacionDao ubicacionDao();

    private static volatile UbicacionDatabase INSTANCE;
    private static final int NUMERO_HILOS = 4;
    
    // Pool de hilos para realizar operaciones en segundo plano
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMERO_HILOS);

    public static UbicacionDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (UbicacionDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    UbicacionDatabase.class, "ubicacion_db")
                            .fallbackToDestructiveMigration()
                            // Callback para carga inicial de datos
                            .addCallback(preCargaDatos)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback para ejecutar código al crear la base de datos, como insertar datos iniciales.
     */
    private static final RoomDatabase.Callback preCargaDatos = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                UbicacionDao dao = INSTANCE.ubicacionDao();
                Log.d("UbicacionDatabase", "Base de datos creada. Insertando ubicaciones de prueba...");
                
                // Insertar ubicaciones de prueba automáticamente
                dao.insertar(new Ubicacion("Casa", "Calle Principal 123", 40.7128, -74.0060, 100, "123456789"));
                dao.insertar(new Ubicacion("Trabajo", "Avenida Empresa 456", 37.7749, -122.4194, 200, "987654321"));
            });
        }
    };
}
