package com.acuimuestra.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.acuimuestra.app.ui.screens.*
import com.acuimuestra.app.viewmodel.RegistroViewModel

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    val volverAlInicio = { nav.popBackStack(Ruta.Inicio.ruta, inclusive = false) }

    NavHost(navController = nav, startDestination = Ruta.Login.ruta) {
        composable(Ruta.Login.ruta) {
            LoginScreen(onLoginExitoso = {
                nav.navigate(Ruta.Inicio.ruta) { popUpTo(Ruta.Login.ruta) { inclusive = true } }
            })
        }
        composable(Ruta.Inicio.ruta) {
            InicioScreen(
                onNuevaMuestra = { nav.navigate(Ruta.Registro.ruta) },
                onHistorial = { nav.navigate(Ruta.Historial.ruta) },
                onCerrarSesion = {
                    nav.navigate(Ruta.Login.ruta) { popUpTo(nav.graph.id) { inclusive = true } }
                }
            )
        }

        navigation(startDestination = Ruta.NuevaMuestra.ruta, route = Ruta.Registro.ruta) {
            composable(Ruta.NuevaMuestra.ruta) { entry ->
                NuevaMuestraScreen(
                    viewModel = entry.registroViewModel(nav),
                    onSiguiente = { nav.navigate(Ruta.Conteo.ruta) },
                    onCancelar = { volverAlInicio() }
                )
            }
            composable(Ruta.Conteo.ruta) { entry ->
                ConteoScreen(
                    viewModel = entry.registroViewModel(nav),
                    onSiguiente = { nav.navigate(Ruta.Fotografia.ruta) },
                    onVolver = { nav.popBackStack() }
                )
            }
            composable(Ruta.Fotografia.ruta) {
                FotografiaScreen(onSiguiente = { nav.navigate(Ruta.Resumen.ruta) })
            }
            composable(Ruta.Resumen.ruta) {
                ResumenScreen(onGuardado = { volverAlInicio() })
            }
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

@Composable
private fun NavBackStackEntry.registroViewModel(nav: NavHostController): RegistroViewModel {
    val padre = remember(this) { nav.getBackStackEntry(Ruta.Registro.ruta) }
    return viewModel(viewModelStoreOwner = padre, factory = RegistroViewModel.Factory)
}