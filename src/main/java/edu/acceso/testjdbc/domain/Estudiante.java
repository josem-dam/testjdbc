package edu.acceso.testjdbc.domain;

import java.time.LocalDate;
import java.time.Period;

public class Estudiante {

    private Integer id; /* Impedir setId si id no es nulo */
    private String nombre;
    private LocalDate nacimiento;
    private Centro centro;

    public Estudiante() {
        super();
    }

    public Estudiante initialize(Integer id, String nombre, LocalDate nacimiento, Centro centro) {
        setId(id);
        setNombre(nombre);
        setNacimiento(nacimiento);
        setCentro(centro);
        return this;
    }

    public Estudiante(Integer id, String nombre, LocalDate nacimiento, Centro centro) {
        initialize(id, nombre, nacimiento, centro);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        if(this.id != null) throw new IllegalStateException("El identificador no puede modificarse.");
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getNacimiento() {
        return nacimiento;
    }

    public void setNacimiento(LocalDate nacimiento) {
        if(nacimiento == null) throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        this.nacimiento = nacimiento;
    }

    public Centro getCentro() {
        return centro;
    }

    public void setCentro(Centro centro) {
        this.centro = centro;
    }

    public int getEdad() {
        return Period.between(nacimiento, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        String nombreCentro = centro == null ? "?":centro.getNombre();
        return String.format("%s (%s, %d años)", nombre, nombreCentro, getEdad());
    }
}
