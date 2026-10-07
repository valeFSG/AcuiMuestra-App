package com.acuimuestra.app.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.acuimuestra.app.AcuiMuestraApp
import com.acuimuestra.app.AppContainer

fun CreationExtras.contenedor(): AppContainer =
    (checkNotNull(this[APPLICATION_KEY]) as AcuiMuestraApp).contenedor