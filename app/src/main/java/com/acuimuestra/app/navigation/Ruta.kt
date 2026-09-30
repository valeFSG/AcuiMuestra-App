package com.acuimuestra.app.navigation

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object Inicio : Ruta("inicio")
    data object NuevaMuestra : Ruta("nueva_muestra")
    data object Conteo : Ruta("conteo")
    data object Fotografia : Ruta("fotografia")
    data object Resumen : Ruta("resumen")
    data object Historial : Ruta("historial")
    data object Detalle : Ruta("detalle/{muestraId}") {
        fun crear(muestraId: Long) = "detalle/$muestraId"
    }
}