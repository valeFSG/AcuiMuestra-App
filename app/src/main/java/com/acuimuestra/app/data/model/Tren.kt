package com.acuimuestra.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trenes",
    foreignKeys = [ForeignKey(
        entity = Centro::class,
        parentColumns = ["id"],
        childColumns = ["centroId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("centroId")]
)
data class Tren(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val centroId: Long,
    val nombre: String
)