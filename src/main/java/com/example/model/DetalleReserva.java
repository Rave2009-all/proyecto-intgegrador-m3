package com.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Detalle_Reserva")
@IdClass(DetalleReservaId.class)
public class DetalleReserva {
    @Id
    private Long reservaId;

    @Id
    private Long libroId;

    @ManyToOne
    @JoinColumn(name = "libroId", insertable = false, updatable = false)
    private Libro libro;

    private Integer Cantidad;
    private String Notas;

    public DetalleReserva() {
    }

    public DetalleReserva(Long reservaId, Libro libro, Integer cantidad, String notas) {
        this.reservaId = reservaId;
        this.libro = libro;
        this.libroId = libro != null ? libro.getId() : null;
        this.Cantidad = cantidad;
        this.Notas = notas;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }

    public Long getLibroId() {
        return libroId;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
        this.libroId = libro != null ? libro.getId() : null;
    }

    public Integer getCantidad() {
        return Cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.Cantidad = cantidad;
    }

    public String getNotas() {
        return Notas;
    }

    public void setNotas(String notas) {
        this.Notas = notas;
    }

    @Override
    public String toString() {
        return "DetalleReserva [reservaId=" + reservaId + ", libroId=" + libroId + ", cantidad=" + Cantidad + "]";
    }
}