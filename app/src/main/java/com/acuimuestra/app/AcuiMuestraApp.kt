package com.acuimuestra.app

import android.app.Application
import com.acuimuestra.app.data.local.AcuiMuestraDatabase
import com.acuimuestra.app.data.local.DatosPrueba
import com.acuimuestra.app.data.remote.ApiCliente
import com.acuimuestra.app.data.remote.RedMonitor
import com.acuimuestra.app.data.remote.RelojRed
import com.acuimuestra.app.data.repository.CatalogoRepository
import com.acuimuestra.app.data.repository.MuestraRepository
import com.acuimuestra.app.data.repository.SincronizacionRepository
import com.acuimuestra.app.data.repository.UsuarioRepository
import com.acuimuestra.app.sync.Sincronizador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(app: Application, db: AcuiMuestraDatabase) {
    val usuarioRepository = UsuarioRepository(db.usuarioDao())
    val catalogoRepository = CatalogoRepository(db.catalogoDao())
    val sincronizador = Sincronizador(app, RedMonitor(app))
    val muestraRepository = MuestraRepository(
        dao = db.muestraDao(),
        usuarioActual = { usuarioRepository.usuarioActual.value?.usuario },
        alCambiar = sincronizador::sincronizar
    )
    val sincronizacionRepository = SincronizacionRepository(
        muestraDao = db.muestraDao(),
        usuarioDao = db.usuarioDao(),
        api = ApiCliente.crear(),
        usuarioConsulta = { usuarioRepository.usuarioActual.value?.usuario ?: "operador" }
    )
    val relojRed = RelojRed()
}

class AcuiMuestraApp : Application() {

    lateinit var contenedor: AppContainer
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val db = AcuiMuestraDatabase.obtener(this)
        contenedor = AppContainer(this, db)
        scope.launch {
            DatosPrueba.cargarSiVacia(db)
            contenedor.sincronizador.sincronizar()
        }
    }
}
