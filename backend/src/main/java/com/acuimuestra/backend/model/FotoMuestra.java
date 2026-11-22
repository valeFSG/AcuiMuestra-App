package com.acuimuestra.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "fotos_muestras")
public class FotoMuestra {

    @Id
    @Column(length = 36)
    private String muestraId;

    @Column(nullable = false, length = 5_000_000)
    private byte[] datos;

    private Long actualizadaEn;

    protected FotoMuestra() {}

    public FotoMuestra(String muestraId, byte[] datos) {
        this.muestraId = muestraId;
        this.datos = datos;
    }

    @PrePersist
    @PreUpdate
    void marcarActualizacion() {
        actualizadaEn = System.currentTimeMillis();
    }

    public String getMuestraId() { return muestraId; }
    public byte[] getDatos() { return datos; }
    public void setDatos(byte[] datos) { this.datos = datos; }
    public Long getActualizadaEn() { return actualizadaEn; }
}
