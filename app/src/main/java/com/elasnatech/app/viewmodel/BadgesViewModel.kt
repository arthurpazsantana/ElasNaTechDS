package com.elasnatech.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elasnatech.app.data.db.AppDatabase
import com.elasnatech.app.data.db.EarnedBadgeEntity
import com.elasnatech.app.data.repository.AppRepository
import com.elasnatech.app.data.repository.RoomAppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BadgesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository: AppRepository = RoomAppRepository(
        db.userProfileDao(),
        db.quizResultDao(),
        db.badgeDao()
    )

    val emblemasConquistados: StateFlow<List<EarnedBadgeEntity>> = repository.observarEmblemas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}