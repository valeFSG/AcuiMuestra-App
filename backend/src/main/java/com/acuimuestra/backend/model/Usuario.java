package com.acuimuestra.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String nombreUsuario;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    protected Usuario() {}

    public Usuario(String nombreUsuario, String nombre, Rol rol) {
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getNombre() { return nombre; }
    public Rol getRol() { return rol; }
}
