package com.acuimuestra.app.data.remote

import com.acuimuestra.app.BuildConfig
import com.acuimuestra.app.data.model.EstadoRevision
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

private const val USUARIO = "X-Usuario"

interface AcuiMuestraApi {

    @GET("api/muestras")
    suspend fun listar(@Header(USUARIO) usuario: String): List<MuestraRemota>

    @POST("api/muestras")
    suspend fun crear(@Header(USUARIO) usuario: String, @Body muestra: MuestraEnvio): MuestraRemota

    @PUT("api/muestras/{id}")
    suspend fun corregir(
        @Header(USUARIO) usuario: String,
        @Path("id") id: String,
        @Body muestra: MuestraEnvio
    ): MuestraRemota

    @PATCH("api/muestras/{id}/estado")
    suspend fun cambiarEstado(
        @Header(USUARIO) usuario: String,
        @Path("id") id: String,
        @Body cambio: CambioEstadoEnvio
    ): MuestraRemota

    @PUT("api/muestras/{id}/foto")
    suspend fun subirFoto(
        @Header(USUARIO) usuario: String,
        @Path("id") id: String,
        @Body foto: RequestBody
    )

    @GET("api/muestras/{id}/foto")
    suspend fun descargarFoto(@Header(USUARIO) usuario: String, @Path("id") id: String): ResponseBody

    @POST("api/muestras/{id}/analisis-ia")
    suspend fun analizarConIa(@Header(USUARIO) usuario: String, @Path("id") id: String): AnalisisIa
}

data class AnalisisIa(
    val conteoEstimado: Int,
    val hayCholgas: Boolean,
    val calidadFoto: String,
    val comentario: String,
    val conteoOperador: Int,
    val diferenciaPorcentaje: Double?,
    val requiereRevision: Boolean,
    val modelo: String
)

data class MuestraEnvio(
    val id: String,
    val centro: String,
    val tren: String,
    val linea: String,
    val tramoCm: Int,
    val conteo: Int,
    val observaciones: String?,
    val calibrePromedioMm: Double?,
    val pesoGramos: Double?,
    val fechaHora: Long,
    val horaVerificada: Boolean,
    val fotoNombre: String?,
    val conteoMarcado: Int? = null
)

data class MuestraRemota(
    val id: String,
    val centro: String,
    val tren: String,
    val linea: String,
    val operador: String,
    val fechaHora: Long,
    val horaVerificada: Boolean,
    val tramoCm: Int,
    val conteo: Int,
    val observaciones: String?,
    val calibrePromedioMm: Double?,
    val pesoGramos: Double?,
    val estado: EstadoRevision,
    val comentarioSupervisor: String?,
    val conteoMarcado: Int? = null,
    val tieneFoto: Boolean = false,
    val revisadoPor: String? = null
)

data class CambioEstadoEnvio(val estado: EstadoRevision, val comentario: String?)

object ApiCliente {
    fun crear(urlBase: String = BuildConfig.API_URL, claveApi: String = BuildConfig.API_KEY): AcuiMuestraApi {
        val registro = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val http = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .addInterceptor { cadena ->
                val peticion = cadena.request()
                cadena.proceed(
                    if (claveApi.isBlank()) peticion
                    else peticion.newBuilder().header("X-Api-Key", claveApi).build()
                )
            }
            .addInterceptor(registro)
            .build()
        return Retrofit.Builder()
            .baseUrl(urlBase)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AcuiMuestraApi::class.java)
    }
}
