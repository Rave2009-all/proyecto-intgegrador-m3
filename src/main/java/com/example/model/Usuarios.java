package com.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Usuarios")
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID_Usuario;
    private String Nombre_Usuario;
    private String Email_Usuario;
    private String Tipo_Usuario;

    // ===================
    // Constructores
    // ===================
    public Usuarios() {
    }


    public Usuarios( String nombre, String email, String tipo) {
        this.ID_Usuario = null;
        this.Nombre_Usuario = nombre;
        this.Email_Usuario = email;
        this.Tipo_Usuario = tipo;
    }

    public Usuarios(Long id, String nombre, String email, String tipo) {
        this.ID_Usuario = id;
        this.Nombre_Usuario = nombre;
        this.Email_Usuario = email;
        this.Tipo_Usuario = tipo;
    }

    // ===================
    // Getters y setters
    // ===================

    public String getemail_Usuario() {
        return Email_Usuario;
    }
    public Long getusuarioId() {
        return ID_Usuario;
    }

    public void setusuarioId(Long id) {
        this.ID_Usuario = id;
    }

    public String getnombreUsuario() {
        return Nombre_Usuario;
    }

    public void setnombreUsuario(String nombre) {
        this.Nombre_Usuario = nombre;
    }

    public String getemailUsuario() {
        return Nombre_Usuario;
    }

    public void setemailUsuario(String email) {
        this.Email_Usuario = email;
    }

    public String gettipoUsuario() {
        return Tipo_Usuario;
    }

    public void settipoUsuario(String tipo) {
        this.Tipo_Usuario = tipo;
    }

  
    @Override
    public String toString() {
        return "Producto [id=" + ID_Usuario + ", codigo=" + Nombre_Usuario + ", nombre=" + Nombre_Usuario + ", stock=" + Tipo_Usuario + "]";
    }
}
