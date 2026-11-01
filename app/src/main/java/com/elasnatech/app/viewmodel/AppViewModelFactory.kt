package com.elasnatech.app.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elasnatech.app.data.db.AppDatabase
import com.elasnatech.app.data.repository.AppRepository
import com.elasnatech.app.data.repository.RoomAppRepository

class AppViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getInstance(application)
        val defaultRepo = repository ?: RoomAppRepository(db.userProfileDao(), db.quizResultDao(), db.badgeDao())

        when {
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(application, defaultRepo) as T
            }
            modelClass.isAssignableFrom(QuizViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return QuizViewModel(application, defaultRepo) as T
            }
            modelClass.isAssignableFrom(BadgesViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return BadgesViewModel(application, defaultRepo) as T
            }
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
    }
}