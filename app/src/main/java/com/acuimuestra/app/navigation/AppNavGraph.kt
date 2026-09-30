package com.acuimuestra.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.acuimuestra.app.ui.screens.*

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Ruta.Login.ruta) {
        composable(Ruta.Login.ruta) {
            LoginScreen(onLoginExitoso = {
                nav.navigate(Ruta.Inicio.ruta) { popUpTo(Ruta.Login.ruta) { inclusive = true } }
            })
        }
        composable(Ruta.Inicio.ruta) {
            InicioScreen(
                onNuevaMuestra = { nav.navigate(Ruta.NuevaMuestra.ruta) },
                onHistorial = { nav.navigate(Ruta.Historial.ruta) }
            )
        }
        composable(Ruta.NuevaMuestra.ruta) { NuevaMuestraScreen(onSiguiente = { nav.navigate(Ruta.Conteo.ruta) }) }
        composable(Ruta.Conteo.ruta) { ConteoScreen(onSiguiente = { nav.navigate(Ruta.Fotografia.ruta) }) }
        composable(Ruta.Fotografia.ruta) { FotografiaScreen(onSiguiente = { nav.navigate(Ruta.Resumen.ruta) }) }
        composable(Ruta.Resumen.ruta) {
            ResumenScreen(onGuardado = { nav.popBackStack(Ruta.Inicio.ruta, inclusive = false) })
        }
        composable(Ruta.Historial.ruta) {
            HistorialScreen(onVerDetalle = { id -> nav.navigate(Ruta.Detalle.crear(id)) })
        }
        composable(
            route = Ruta.Detalle.ruta,
            arguments = listOf(navArgument("muestraId") { type = NavType.LongType })
        ) { entry ->
            DetalleScreen(
                muestraId = entry.arguments?.getLong("muestraId") ?: 0L,
                onVolver = { nav.popBackStack() }
            )
        }
    }
}