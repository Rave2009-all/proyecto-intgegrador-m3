package com.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "Libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID_Libro;
    private String Titulo;
    private String Autor;

        @Column(length = 1000)
    private String portada;
   

    @ManyToOne
    @JoinColumn(name = "Editorial_ID")
    private Editorial editorial;

    @ManyToOne
    @JoinColumn(name = "Categoria_ID")
    private Categoria categoria;

    public Libro() {
    }

    public Libro(String titulo, String autor, Editorial editorial, Categoria categoria) {
        this.ID_Libro = null;
        this.Titulo = titulo;
        this.Autor = autor;
        this.editorial = editorial;
        this.categoria = categoria;
    }

    public Libro(Long id, String titulo, String autor, Editorial editorial, Categoria categoria) {
        this.ID_Libro = id;
        this.Titulo = titulo;
        this.Autor = autor;
        this.editorial = editorial;
        this.categoria = categoria;
    }

    public Long getId() {
        return ID_Libro;
    }

    public void setId(Long id) {
        this.ID_Libro = id;
    }

    public String getTitulo() {
        return Titulo;
    }

    public void setTitulo(String titulo) {
        this.Titulo = titulo;
    }

    public String getAutor() {
        return Autor;
    }

    public void setAutor(String autor) {
        this.Autor = autor;
    }

    public Editorial getEditorial() {
        return editorial;
    }

    public void setEditorial(Editorial editorial) {
        this.editorial = editorial;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getPortada() {
        return portada;
    }

    public void setPortada(String portada) {
        this.portada = portada;
    }

    @Override
    public String toString() {
        return "Libro [id=" + ID_Libro + ", titulo=" + Titulo + ", autor=" + Autor + "]";
    }
}