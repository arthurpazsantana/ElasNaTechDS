package com.elasnatech.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elasnatech.app.data.db.AppDatabase
import com.elasnatech.app.data.repository.AppRepository
import com.elasnatech.app.data.db.UserProfileEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = AppRepository(
        db.userProfileDao(),
        db.quizResultDao(),
        db.badgeDao()
    )

    // Expõe o perfil reativamente para a tela (UI)
    val perfil: StateFlow<UserProfileEntity?> = repository.observarPerfil()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun atualizarPerfil(nome: String, avatarId: Int) {
        viewModelScope.launch {
            repository.salvarPerfil(nome, avatarId)
        }
    }

    fun calcularNivelAtual(xp: Int): Int {
        return repository.calcularNivel(xp)
    }
}