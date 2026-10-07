package com.acuimuestra.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "muestras",
    foreignKeys = [
        ForeignKey(entity = Centro::class, parentColumns = ["id"], childColumns = ["centroId"]),
        ForeignKey(entity = Tren::class, parentColumns = ["id"], childColumns = ["trenId"]),
        ForeignKey(entity = Linea::class, parentColumns = ["id"], childColumns = ["lineaId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["operadorId"])
    ],
    indices = [Index("centroId"), Index("trenId"), Index("lineaId"), Index("operadorId"), Index("estado")]
)
data class Muestra(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val centroId: Long,
    val trenId: Long,
    val lineaId: Long,
    val operadorId: Long,
    val fechaHora: Long,
    val tramoCm: Int,
    val conteo: Int,
    val observaciones: String = "",
    val fotoUri: String? = null,
    val calibrePromedioMm: Double? = null,
    val calibreMinMm: Double? = null,
    val calibreMaxMm: Double? = null,
    val pesoGramos: Double? = null,
    val estado: EstadoRevision = EstadoRevision.PENDIENTE,
    val comentarioSupervisor: String? = null
)