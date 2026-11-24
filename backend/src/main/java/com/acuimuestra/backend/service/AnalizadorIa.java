package com.acuimuestra.backend.service;

public interface AnalizadorIa {
    boolean configurado();
    String modelo();
    ResultadoIa analizar(byte[] fotoJpeg);
}
