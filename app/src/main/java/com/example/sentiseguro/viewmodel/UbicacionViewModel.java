package com.example.sentiseguro.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.sentiseguro.baseDatos.Ubicacion;
import com.example.sentiseguro.baseDatos.UbicacionDatabase;
import com.example.sentiseguro.baseDatos.UbicacionDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UbicacionViewModel extends AndroidViewModel {

    private final UbicacionDao ubicacionDao;
    private final LiveData<List<Ubicacion>> todasLasUbicaciones;
    private final ExecutorService executorService;

    public UbicacionViewModel(@NonNull Application application) {
        super(application);
        UbicacionDatabase db = UbicacionDatabase.getInstance(application);
        ubicacionDao = db.ubicacionDao();
        todasLasUbicaciones = ubicacionDao.obtenerTodas(); // LiveData para actualización automática
        executorService = Executors.newSingleThreadExecutor(); // Para operaciones en segundo plano
    }

    /**
     * Retorna todas las ubicaciones en tiempo real.
     */
    public LiveData<List<Ubicacion>> getUbicaciones() {
        return todasLasUbicaciones;
    }

    /**
     * Inserta una nueva ubicación en la base de datos.
     */
    public void agregarUbicacion(Ubicacion ubicacion) {
        executorService.execute(() -> ubicacionDao.insertar(ubicacion));
    }

    /**
     * Actualiza una ubicación en la base de datos.
     */
    public void actualizarUbicacion(Ubicacion ubicacion) {
        executorService.execute(() -> ubicacionDao.actualizar(ubicacion));
    }

    /**
     * Elimina una ubicación de la base de datos.
     */
    public void eliminarUbicacion(Ubicacion ubicacion) {
        executorService.execute(() -> ubicacionDao.eliminar(ubicacion));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Cerrar el ExecutorService para liberar recursos
        executorService.shutdown();
    }
}
