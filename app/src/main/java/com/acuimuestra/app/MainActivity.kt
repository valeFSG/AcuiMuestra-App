package com.acuimuestra.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acuimuestra.app.data.repository.ModoTema
import com.acuimuestra.app.navigation.AppNavGraph
import com.acuimuestra.app.ui.theme.AcuiMuestraTheme
import com.acuimuestra.app.viewmodel.TemaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val tema: TemaViewModel = viewModel(factory = TemaViewModel.Factory)
            val modo by tema.modo.collectAsStateWithLifecycle()
            val oscuro = when (modo) {
                ModoTema.SISTEMA -> isSystemInDarkTheme()
                ModoTema.CLARO -> false
                ModoTema.OSCURO -> true
            }
            AcuiMuestraTheme(darkTheme = oscuro) {
                Surface(modifier = Modifier.fillMaxSize()) { AppNavGraph() }
            }
        }
    }
}