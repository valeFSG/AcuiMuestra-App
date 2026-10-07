package com.acuimuestra.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.acuimuestra.app.data.model.Usuario

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE usuario = :usuario AND clave = :clave LIMIT 1")
    suspend fun autenticar(usuario: String, clave: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(usuarios: List<Usuario>)
}