package com.acuimuestra.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.acuimuestra.app.data.model.Centro
import com.acuimuestra.app.data.model.Linea
import com.acuimuestra.app.data.model.Muestra
import com.acuimuestra.app.data.model.Tren
import com.acuimuestra.app.data.model.Usuario

@Database(
    entities = [Usuario::class, Centro::class, Tren::class, Linea::class, Muestra::class],
    version = 1,
    exportSchema = false
)
abstract class AcuiMuestraDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun catalogoDao(): CatalogoDao
    abstract fun muestraDao(): MuestraDao

    companion object {
        @Volatile private var instancia: AcuiMuestraDatabase? = null

        fun obtener(context: Context): AcuiMuestraDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AcuiMuestraDatabase::class.java,
                    "acuimuestra.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }
}