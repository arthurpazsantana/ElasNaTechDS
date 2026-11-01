package com.elasnatech.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elasnatech.app.data.db.AppDatabase
import com.elasnatech.app.data.repository.AppRepository
import com.elasnatech.app.data.repository.RoomAppRepository
import com.elasnatech.app.data.repository.EarnedBadge
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BadgesViewModel(application: Application, private val repository: AppRepository) : AndroidViewModel(application) {

    // Construtor secundário padrão
    constructor(application: Application) : this(
        application,
        RoomAppRepository(
            AppDatabase.getInstance(application).userProfileDao(),
            AppDatabase.getInstance(application).quizResultDao(),
            AppDatabase.getInstance(application).badgeDao()
        )
    )

    val emblemasConquistados: StateFlow<List<EarnedBadge>> = repository.observarEmblemas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}