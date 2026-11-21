package com.acuimuestra.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.acuimuestra.app.AcuiMuestraApp
import retrofit2.HttpException
import java.io.IOException

class SincronizacionWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repositorio = (applicationContext as AcuiMuestraApp).contenedor.sincronizacionRepository
        return try {
            repositorio.sincronizar()
            Result.success()
        } catch (e: IOException) {
            Result.retry()
        } catch (e: HttpException) {
            if (e.code() >= 500) Result.retry() else Result.failure()
        }
    }
}
