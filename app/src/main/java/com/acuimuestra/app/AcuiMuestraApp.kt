package com.acuimuestra.app

import android.app.Application
import com.acuimuestra.app.data.local.AcuiMuestraDatabase
import com.acuimuestra.app.data.local.DatosPrueba
import com.acuimuestra.app.data.repository.CatalogoRepository
import com.acuimuestra.app.data.repository.MuestraRepository
import com.acuimuestra.app.data.repository.UsuarioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(db: AcuiMuestraDatabase) {
    val usuarioRepository = UsuarioRepository(db.usuarioDao())
    val catalogoRepository = CatalogoRepository(db.catalogoDao())
    val muestraRepository = MuestraRepository(db.muestraDao())
}

class AcuiMuestraApp : Application() {

    lateinit var contenedor: AppContainer
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val db = AcuiMuestraDatabase.obtener(this)
        contenedor = AppContainer(db)
        scope.launch { DatosPrueba.cargarSiVacia(db) }
    }
}