package com.acuimuestra.app.data.repository

import com.acuimuestra.app.data.local.AlmacenFotos
import com.acuimuestra.app.data.local.MuestraDao
import com.acuimuestra.app.data.local.UsuarioDao
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.Muestra
import com.acuimuestra.app.data.model.MuestraPendiente
import com.acuimuestra.app.data.remote.AcuiMuestraApi
import com.acuimuestra.app.data.remote.CambioEstadoEnvio
import com.acuimuestra.app.data.remote.MuestraEnvio
import com.acuimuestra.app.data.remote.MuestraRemota
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

data class ResultadoSincronizacion(val enviadas: Int, val recibidas: Int)

class SincronizacionRepository(
    private val muestraDao: MuestraDao,
    private val usuarioDao: UsuarioDao,
    private val api: AcuiMuestraApi,
    private val fotos: AlmacenFotos,
    private val usuarioConsulta: () -> String
) {

    suspend fun sincronizar(): ResultadoSincronizacion = ResultadoSincronizacion(enviar(), recibir())

    suspend fun enviar(): Int {
        var enviadas = 0
        for (pendiente in muestraDao.pendientes()) {
            val m = pendiente.muestra
            val datos = pendiente.aEnvio()
            if (!aceptadaPorServidor { api.crear(pendiente.operadorUsuario, datos) }) continue
            m.fotoUri?.let { subirFoto(pendiente.operadorUsuario, m.uuid, it) }
            when (m.estado) {
                EstadoRevision.OBSERVADO, EstadoRevision.VALIDADO, EstadoRevision.DESCARTADO ->
                    aceptadaPorServidor {
                        api.cambiarEstado(
                            m.revisadoPor ?: m.usuarioCambio ?: SUPERVISOR_POR_DEFECTO,
                            m.uuid,
                            CambioEstadoEnvio(m.estado, m.comentarioSupervisor)
                        )
                    }
                EstadoRevision.CORREGIDO -> aceptadaPorServidor { api.corregir(pendiente.operadorUsuario, m.uuid, datos) }
                EstadoRevision.PENDIENTE -> Unit
            }
            muestraDao.marcarSincronizada(m.uuid)
            enviadas++
        }
        return enviadas
    }

    suspend fun recibir(): Int {
        var recibidas = 0
        for (remota in api.listar(usuarioConsulta())) {
            val local = muestraDao.buscarPorUuid(remota.id)
            if (local != null && !local.sincronizada) continue
            val nueva = aLocal(remota, local) ?: continue
            when {
                local == null -> muestraDao.insertar(nueva)
                nueva != local -> muestraDao.actualizar(nueva)
                else -> continue
            }
            recibidas++
        }
        return recibidas
    }

    private suspend fun aLocal(remota: MuestraRemota, local: Muestra?): Muestra? {
        val ubicacion = muestraDao.buscarUbicacion(remota.centro, remota.tren, remota.linea) ?: return null
        val operador = usuarioDao.buscarPorUsuario(remota.operador) ?: return null
        return Muestra(
            id = local?.id ?: 0,
            uuid = remota.id,
            centroId = ubicacion.centroId,
            trenId = ubicacion.trenId,
            lineaId = ubicacion.lineaId,
            operadorId = operador.id,
            fechaHora = remota.fechaHora,
            horaVerificada = remota.horaVerificada,
            tramoCm = remota.tramoCm,
            conteo = remota.conteo,
            observaciones = remota.observaciones.orEmpty(),
            fotoUri = local?.fotoUri ?: descargarFoto(remota),
            calibrePromedioMm = remota.calibrePromedioMm,
            calibreMinMm = local?.calibreMinMm,
            calibreMaxMm = local?.calibreMaxMm,
            pesoGramos = remota.pesoGramos,
            estado = remota.estado,
            comentarioSupervisor = remota.comentarioSupervisor,
            sincronizada = true,
            usuarioCambio = local?.usuarioCambio,
            revisadoPor = remota.revisadoPor ?: local?.revisadoPor,
            conteoMarcado = remota.conteoMarcado
        )
    }

    private suspend fun subirFoto(usuario: String, uuid: String, uri: String) {
        val datos = fotos.leerParaEnviar(uri) ?: return
        aceptadaPorServidor { api.subirFoto(usuario, uuid, datos.toRequestBody(JPEG)) }
    }

    private suspend fun descargarFoto(remota: MuestraRemota): String? {
        if (!remota.tieneFoto) return null
        val datos = try {
            api.descargarFoto(usuarioConsulta(), remota.id).bytes()
        } catch (e: HttpException) {
            return null
        } catch (e: IOException) {
            return null
        }
        return fotos.guardarRecibida(remota.id, datos)
    }

    private suspend fun aceptadaPorServidor(llamada: suspend () -> Unit): Boolean =
        try {
            llamada()
            true
        } catch (e: HttpException) {
            if (e.code() >= 500) throw e
            e.code() == CONFLICTO
        }

    companion object {
        const val SUPERVISOR_POR_DEFECTO = "supervisor"
        private const val CONFLICTO = 409
        private val JPEG = "image/jpeg".toMediaType()
    }
}

fun MuestraPendiente.aEnvio() = MuestraEnvio(
    id = muestra.uuid,
    centro = centroNombre,
    tren = trenNombre,
    linea = lineaNombre,
    tramoCm = muestra.tramoCm,
    conteo = muestra.conteo,
    observaciones = muestra.observaciones.ifBlank { null },
    calibrePromedioMm = muestra.calibrePromedioMm,
    pesoGramos = muestra.pesoGramos,
    fechaHora = muestra.fechaHora,
    horaVerificada = muestra.horaVerificada,
    fotoNombre = muestra.fotoUri?.substringAfterLast('/'),
    conteoMarcado = muestra.conteoMarcado
)
