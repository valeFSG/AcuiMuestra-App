package com.acuimuestra.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lineas",
    foreignKeys = [ForeignKey(
        entity = Tren::class,
        parentColumns = ["id"],
        childColumns = ["trenId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("trenId")]
)
data class Linea(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trenId: Long,
    val nombre: String
)