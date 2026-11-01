package com.elasnatech.app.viewmodel

import androidx.lifecycle.ViewModel
import com.elasnatech.app.data.repository.AppRepository

class ProfileViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    fun calcularNivel(xpAtual: Int): Int {
        return repository.calcularNivel(xpAtual)
    }
}