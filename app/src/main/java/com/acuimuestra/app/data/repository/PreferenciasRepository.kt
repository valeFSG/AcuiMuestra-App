package com.acuimuestra.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ModoTema {
    SISTEMA, CLARO, OSCURO;

    fun siguiente(): ModoTema = entries[(ordinal + 1) % entries.size]
}

class PreferenciasRepository(private val dataStore: DataStore<Preferences>) {

    val modoTema: Flow<ModoTema> = dataStore.data.map { preferencias ->
        preferencias[CLAVE_MODO_TEMA]
            ?.let { guardado -> ModoTema.entries.firstOrNull { it.name == guardado } }
            ?: ModoTema.SISTEMA
    }

    suspend fun guardarModoTema(modo: ModoTema) {
        dataStore.edit { it[CLAVE_MODO_TEMA] = modo.name }
    }

    companion object {
        internal val CLAVE_MODO_TEMA = stringPreferencesKey("modo_tema")
    }
}
