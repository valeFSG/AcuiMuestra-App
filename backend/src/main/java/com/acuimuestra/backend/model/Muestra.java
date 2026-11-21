package com.acuimuestra.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "muestras", indexes = {
        @Index(name = "idx_muestra_centro", columnList = "centro"),
        @Index(name = "idx_muestra_estado", columnList = "estado")
})
public class Muestra {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 80)
    private String centro;

    @Column(nullable = false, length = 40)
    private String tren;

    @Column(nullable = false, length = 40)
    private String linea;

    @Column(nullable = false, length = 40)
    private String operador;

    @Column(nullable = false)
    private Long fechaHora;

    private boolean horaVerificada = true;

    private int tramoCm;

    private int conteo;

    @Column(length = 200)
    private String observaciones;

    private Double calibrePromedioMm;

    private Double pesoGramos;

    @Column(length = 120)
    private String fotoNombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRevision estado = EstadoRevision.PENDIENTE;

    @Column(length = 500)
    private String comentarioSupervisor;

    private Long actualizadaEn;

    protected Muestra() {}

    public Muestra(String id) {
        this.id = id;
    }

    @PrePersist
    @PreUpdate
    void marcarActualizacion() {
        actualizadaEn = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getCentro() { return centro; }
    public void setCentro(String centro) { this.centro = centro; }
    public String getTren() { return tren; }
    public void setTren(String tren) { this.tren = tren; }
    public String getLinea() { return linea; }
    public void setLinea(String linea) { this.linea = linea; }
    public String getOperador() { return operador; }
    public void setOperador(String operador) { this.operador = operador; }
    public Long getFechaHora() { return fechaHora; }
    public void setFechaHora(Long fechaHora) { this.fechaHora = fechaHora; }
    public boolean isHoraVerificada() { return horaVerificada; }
    public void setHoraVerificada(boolean horaVerificada) { this.horaVerificada = horaVerificada; }
    public int getTramoCm() { return tramoCm; }
    public void setTramoCm(int tramoCm) { this.tramoCm = tramoCm; }
    public int getConteo() { return conteo; }
    public void setConteo(int conteo) { this.conteo = conteo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Double getCalibrePromedioMm() { return calibrePromedioMm; }
    public void setCalibrePromedioMm(Double calibrePromedioMm) { this.calibrePromedioMm = calibrePromedioMm; }
    public Double getPesoGramos() { return pesoGramos; }
    public void setPesoGramos(Double pesoGramos) { this.pesoGramos = pesoGramos; }
    public String getFotoNombre() { return fotoNombre; }
    public void setFotoNombre(String fotoNombre) { this.fotoNombre = fotoNombre; }
    public EstadoRevision getEstado() { return estado; }
    public void setEstado(EstadoRevision estado) { this.estado = estado; }
    public String getComentarioSupervisor() { return comentarioSupervisor; }
    public void setComentarioSupervisor(String comentarioSupervisor) { this.comentarioSupervisor = comentarioSupervisor; }
    public Long getActualizadaEn() { return actualizadaEn; }
}
