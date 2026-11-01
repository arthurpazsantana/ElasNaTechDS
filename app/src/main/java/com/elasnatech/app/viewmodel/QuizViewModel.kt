package com.elasnatech.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elasnatech.app.data.db.AppDatabase
import com.elasnatech.app.data.repository.AppRepository
import com.elasnatech.app.data.repository.RoomAppRepository
import com.elasnatech.app.data.repository.ResultadoQuizModel
import com.elasnatech.app.data.repository.QuizResult
import com.elasnatech.app.QuizQuestion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuizViewModel(application: Application, private val repository: AppRepository) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        RoomAppRepository(
            AppDatabase.getInstance(application).userProfileDao(),
            AppDatabase.getInstance(application).quizResultDao(),
            AppDatabase.getInstance(application).badgeDao()
        )
    )

    val historicoQuizzes = repository.observarHistoricoQuizzes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun carregarPerguntas(): List<QuizQuestion> {
        return repository.obterPerguntasQuiz()
    }

    fun finalizarQuiz(quizId: Int, acertos: Int, totalPerguntas: Int, onResultadoPronto: (ResultadoQuizModel) -> Unit) {
        viewModelScope.launch {
            val dataHoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val resultado = repository.salvarResultadoQuiz(quizId, acertos, totalPerguntas, dataHoje)
            onResultadoPronto(resultado)
        }
    }

    fun obterMelhorResultado(quizId: Int, onResult: (QuizResult?) -> Unit) {
        viewModelScope.launch {
            val melhor = repository.obterMelhorResultadoQuiz(quizId)
            onResult(melhor)
        }
    }
}