package com.acuimuestra.app.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface MarinaApi {
    @GET("v1/marine")
    suspend fun condicionesActuales(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") variables: String = "wave_height,sea_surface_temperature",
        @Query("timezone") zonaHoraria: String = "America/Santiago"
    ): RespuestaMarina
}

data class RespuestaMarina(val current: ActualMarina?)

data class ActualMarina(
    val time: String?,
    @SerializedName("wave_height") val alturaOlaM: Double?,
    @SerializedName("sea_surface_temperature") val temperaturaMarC: Double?
)

object MarinaCliente {
    private const val URL_BASE = "https://marine-api.open-meteo.com/"

    fun crear(urlBase: String = URL_BASE): MarinaApi {
        val http = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(urlBase)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarinaApi::class.java)
    }
}