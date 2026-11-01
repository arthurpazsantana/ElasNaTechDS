package com.elasnatech.app.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elasnatech.app.data.db.AppDatabase
import com.elasnatech.app.data.repository.AppRepository
import com.elasnatech.app.data.repository.RoomAppRepository

class AppViewModelFactory(
    private val application: Application,
    private val repository: AppRepository? = null
) : ViewModelProvider.Factory {

    // Inicializa o Room apenas se nenhum repositório (ex: Fake) for fornecido
    private val resolvedRepository by lazy {
        repository ?: run {
            val db = AppDatabase.getInstance(application)
            RoomAppRepository(db.userProfileDao(), db.quizResultDao(), db.badgeDao())
        }
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(application, resolvedRepository) as T
            }
            modelClass.isAssignableFrom(QuizViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return QuizViewModel(application, resolvedRepository) as T
            }
            modelClass.isAssignableFrom(BadgesViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return BadgesViewModel(application, resolvedRepository) as T
            }
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
    }
}