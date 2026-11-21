package com.acuimuestra.app

import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.Muestra
import com.acuimuestra.app.data.model.MuestraDetalle
import com.acuimuestra.app.data.model.MuestraResumen
import com.acuimuestra.app.data.model.Rol
import com.acuimuestra.app.data.model.Usuario

object Datos {
    val operador = Usuario(id = 1, usuario = "operador", clave = "1234", nombreVisible = "Operador", rol = Rol.OPERADOR)
    val supervisor = Usuario(id = 2, usuario = "supervisor", clave = "1234", nombreVisible = "Supervisor", rol = Rol.SUPERVISOR)

    fun muestra(
        id: Long = 7,
        uuid: String = "u-7",
        estado: EstadoRevision = EstadoRevision.PENDIENTE,
        sincronizada: Boolean = false
    ) = Muestra(
        id = id,
        uuid = uuid,
        centroId = 1,
        trenId = 1,
        lineaId = 1,
        operadorId = 1,
        fechaHora = 1_000,
        tramoCm = 30,
        conteo = 185,
        observaciones = "Sin daños",
        fotoUri = "file:///fotos/muestra7.jpg",
        calibrePromedioMm = 42.5,
        pesoGramos = 1200.0,
        estado = estado,
        sincronizada = sincronizada
    )

    fun detalle(estado: EstadoRevision = EstadoRevision.PENDIENTE, comentario: String? = null) = MuestraDetalle(
        id = 7,
        uuid = "u-7",
        fechaHora = 1_000,
        tramoCm = 30,
        conteo = 185,
        observaciones = "Sin daños",
        fotoUri = "file:///fotos/muestra7.jpg",
        calibrePromedioMm = 42.5,
        calibreMinMm = null,
        calibreMaxMm = null,
        pesoGramos = 1200.0,
        estado = estado,
        comentarioSupervisor = comentario,
        centroNombre = "Centro Ficticio Sur",
        trenNombre = "Tren 1",
        lineaNombre = "Línea 2",
        operadorNombre = "Operador",
        horaVerificada = true,
        sincronizada = true
    )

    fun resumen(
        id: Long,
        estado: EstadoRevision = EstadoRevision.PENDIENTE,
        centro: String = "Centro Ficticio Norte"
    ) = MuestraResumen(
        id = id,
        fechaHora = 1_000L * id,
        conteo = 100 + id.toInt(),
        estado = estado,
        centroNombre = centro,
        trenNombre = "Tren 1",
        lineaNombre = "Línea 1",
        operadorNombre = "Operador",
        sincronizada = true,
        tramoCm = 20
    )
}
