package com.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Categorias")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID_Categoria;
    private String Nombre_Categoria;

    public Categoria() {
    }

    public Categoria(String nombre) {
        this.ID_Categoria = null;
        this.Nombre_Categoria = nombre;
    }

    public Categoria(Long id, String nombre) {
        this.ID_Categoria = id;
        this.Nombre_Categoria = nombre;
    }

    public Long getId() {
        return ID_Categoria;
    }

    public void setId(Long id) {
        this.ID_Categoria = id;
    }

    public String getNombreCategoria() {
        return Nombre_Categoria;
    }

    public void setNombreCategoria(String nombre) {
        this.Nombre_Categoria = nombre;
    }

    @Override
    public String toString() {
        return "Categoria [id=" + ID_Categoria + ", nombre=" + Nombre_Categoria + "]";
    }
}
