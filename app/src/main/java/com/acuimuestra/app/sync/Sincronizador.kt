package com.acuimuestra.app.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.acuimuestra.app.data.remote.RedMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

interface ControlSincronizacion {
    val enLinea: Flow<Boolean>
    val sincronizando: Flow<Boolean>
    fun sincronizar()
}

class Sincronizador(context: Context, red: RedMonitor) : ControlSincronizacion {

    private val workManager = WorkManager.getInstance(context)

    override val enLinea: Flow<Boolean> = red.enLinea

    override val sincronizando: Flow<Boolean> =
        workManager.getWorkInfosForUniqueWorkFlow(NOMBRE)
            .map { trabajos -> trabajos.any { it.state == WorkInfo.State.RUNNING } }

    override fun sincronizar() {
        val trabajo = OneTimeWorkRequestBuilder<SincronizacionWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(NOMBRE, ExistingWorkPolicy.APPEND_OR_REPLACE, trabajo)
    }

    companion object {
        const val NOMBRE = "sincronizacion-muestras"
    }
}
