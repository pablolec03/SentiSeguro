package com.example.sentiseguro.baseDatos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UbicacionDao {

    // Insertar nueva ubicación.
    @Insert
    void insertar(Ubicacion ubicacion);

    // Actualizar una ubicación existente.
    @Update
    void actualizar(Ubicacion ubicacion);

    // Eliminar una ubicación específica.
    @Delete
    void eliminar(Ubicacion ubicacion);

    // Método síncrono para obtener todas las ubicaciones (usado en BootReceiver).
    @Query("SELECT * FROM ubicacion ORDER BY nombre ASC")
    List<Ubicacion> obtenerTodasSync();

    // Método asincrónico para obtener todas las ubicaciones en tiempo real (LiveData).
    @Query("SELECT * FROM ubicacion ORDER BY nombre ASC")
    LiveData<List<Ubicacion>> obtenerTodas();

    // Obtener una ubicación específica por su ID.
    @Query("SELECT * FROM ubicacion WHERE id = :id")
    LiveData<Ubicacion> obtenerPorId(int id);

    // Buscar ubicaciones por nombre o dirección (para la función de búsqueda en RecyclerView).
    @Query("SELECT * FROM ubicacion WHERE nombre LIKE '%' || :query || '%' OR direccion LIKE '%' || :query || '%' ORDER BY nombre ASC")
    LiveData<List<Ubicacion>> buscarUbicaciones(String query);

    // Obtener ubicaciones dentro de un radio en metros (para Geofencing).
    @Query("SELECT * FROM ubicacion WHERE (latitud BETWEEN :latMin AND :latMax) AND (longitud BETWEEN :lonMin AND :lonMax)")
    LiveData<List<Ubicacion>> obtenerUbicacionesEnRadio(double latMin, double latMax, double lonMin, double lonMax);

    // Eliminar todas las ubicaciones (para pruebas o reinicio de datos).
    @Query("DELETE FROM ubicacion")
    void eliminarTodas();
}
