package com.acuimuestra.backend.service;

public record ResultadoIa(int conteoEstimado, boolean hayCholgas, String calidadFoto, String comentario, String modelo) {

    public ResultadoIa(int conteoEstimado, boolean hayCholgas, String calidadFoto, String comentario) {
        this(conteoEstimado, hayCholgas, calidadFoto, comentario, null);
    }

    ResultadoIa conModelo(String modelo) {
        return new ResultadoIa(conteoEstimado, hayCholgas, calidadFoto, comentario, modelo);
    }
}
