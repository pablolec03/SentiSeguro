package com.example.sentiseguro.baseDatos;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "ubicacion")
public class Ubicacion implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nombre;
    private String direccion;
    private double latitud;  
    private double longitud; 
    private int rango;
    private String contacto;

    // Constructor vacío requerido por Room
    public Ubicacion() { }

    // Constructor principal
    public Ubicacion(String nombre, String direccion, double latitud, double longitud, int rango, String contacto) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.rango = rango;
        this.contacto = contacto;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public int getRango() { return rango; }
    public void setRango(int rango) { this.rango = rango; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    // Implementación de Parcelable para pasar objetos entre actividades
    @Ignore
    protected Ubicacion(Parcel in) {
        id = in.readInt();
        nombre = in.readString();
        direccion = in.readString();
        latitud = in.readDouble();
        longitud = in.readDouble();
        rango = in.readInt();
        contacto = in.readString();
    }

    public static final Creator<Ubicacion> CREATOR = new Creator<Ubicacion>() {
        @Override
        public Ubicacion createFromParcel(Parcel in) {
            return new Ubicacion(in);
        }

        @Override
        public Ubicacion[] newArray(int size) {
            return new Ubicacion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nombre);
        dest.writeString(direccion);
        dest.writeDouble(latitud);
        dest.writeDouble(longitud);
        dest.writeInt(rango);
        dest.writeString(contacto);
    }
}
