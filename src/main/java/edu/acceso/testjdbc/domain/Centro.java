package edu.acceso.testjdbc.domain;

public class Centro {

    private int id;
    private String nombre;
    private Titularidad titularidad;

    public Centro() {
        super();
    }

    public Centro initialize(int id, String nombre, Titularidad titularidad) {
        setId(id);
        setNombre(nombre);
        setTitularidad(titularidad);
        return this;
    }

    public Centro(int id, String nombre, Titularidad titularidad) {
        initialize(id, nombre, titularidad);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if(this.id != 0) throw new IllegalStateException("El identificador no puede modificarse.");
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Titularidad getTitularidad() {
        return titularidad;
    }

    public void setTitularidad(Titularidad titularidad) {
        this.titularidad = titularidad;
    }

    
    @Override
    public String toString() {
        return String.format("%s [%d, %s]", nombre, id, titularidad);
    }
}
