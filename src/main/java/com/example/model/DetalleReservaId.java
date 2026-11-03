package com.example.model;

import java.io.Serializable;
import java.util.Objects;

public class DetalleReservaId implements Serializable {
    private Long reservaId;
    private Long libroId;

    public DetalleReservaId() {
    }

    public DetalleReservaId(Long reservaId, Long libroId) {
        this.reservaId = reservaId;
        this.libroId = libroId;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public Long getLibroId() {
        return libroId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetalleReservaId)) return false;
        DetalleReservaId that = (DetalleReservaId) o;
        return Objects.equals(reservaId, that.reservaId) && Objects.equals(libroId, that.libroId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservaId, libroId);
    }
}
