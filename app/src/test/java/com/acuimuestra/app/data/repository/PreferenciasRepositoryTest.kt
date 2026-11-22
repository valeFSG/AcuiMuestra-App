package com.acuimuestra.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferenciasRepositoryTest {

    private val dataStore = mockk<DataStore<Preferences>>()

    private fun repositorioCon(preferencias: Preferences): PreferenciasRepository {
        every { dataStore.data } returns flowOf(preferencias)
        return PreferenciasRepository(dataStore)
    }

    @Test
    fun sinPreferenciaGuardada_usaElTemaDelSistema() = runTest {
        assertEquals(ModoTema.SISTEMA, repositorioCon(emptyPreferences()).modoTema.first())
    }

    @Test
    fun leeElModoGuardado() = runTest {
        val preferencias = preferencesOf(PreferenciasRepository.CLAVE_MODO_TEMA to "OSCURO")
        assertEquals(ModoTema.OSCURO, repositorioCon(preferencias).modoTema.first())
    }

    @Test
    fun valorDesconocido_vuelveAlTemaDelSistema() = runTest {
        val preferencias = preferencesOf(PreferenciasRepository.CLAVE_MODO_TEMA to "AZUL")
        assertEquals(ModoTema.SISTEMA, repositorioCon(preferencias).modoTema.first())
    }

    @Test
    fun guardarModo_loEscribeEnDataStore() = runTest {
        val guardadas = mutableListOf<Preferences>()
        coEvery { dataStore.updateData(any()) } coAnswers {
            val transformar = firstArg<suspend (Preferences) -> Preferences>()
            transformar(emptyPreferences()).also { guardadas += it }
        }

        repositorioCon(emptyPreferences()).guardarModoTema(ModoTema.CLARO)

        assertEquals("CLARO", guardadas.single()[PreferenciasRepository.CLAVE_MODO_TEMA])
    }

    @Test
    fun siguiente_recorreLosTresModos() {
        assertEquals(ModoTema.CLARO, ModoTema.SISTEMA.siguiente())
        assertEquals(ModoTema.OSCURO, ModoTema.CLARO.siguiente())
        assertEquals(ModoTema.SISTEMA, ModoTema.OSCURO.siguiente())
    }
}
