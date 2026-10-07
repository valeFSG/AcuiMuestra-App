package com.acuimuestra.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "centros")
data class Centro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)