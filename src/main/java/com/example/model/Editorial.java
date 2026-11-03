package com.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Editoriales")
public class Editorial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID_Editorial;
    private String Nombre_Editorial;

    public Editorial() {
    }

    public Editorial(String nombre) {
        this.ID_Editorial = null;
        this.Nombre_Editorial = nombre;
    }

    public Editorial(Long id, String nombre) {
        this.ID_Editorial = id;
        this.Nombre_Editorial = nombre;
    }

    public Long getId() {
        return ID_Editorial;
    }

    public void setId(Long id) {
        this.ID_Editorial = id;
    }

    public String getNombreEditorial() {
        return Nombre_Editorial;
    }

    public void setNombreEditorial(String nombre) {
        this.Nombre_Editorial = nombre;
    }

    @Override
    public String toString() {
        return "Editorial [id=" + ID_Editorial + ", nombre=" + Nombre_Editorial + "]";
    }
}
