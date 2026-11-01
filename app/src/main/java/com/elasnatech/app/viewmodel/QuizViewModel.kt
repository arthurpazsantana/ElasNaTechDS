package com.elasnatech.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elasnatech.app.data.db.AppDatabase
import com.elasnatech.app.data.repository.AppRepository
import com.elasnatech.app.data.repository.ResultadoQuizModel
import com.elasnatech.app.QuizQuestion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = AppRepository(
        db.userProfileDao(),
        db.quizResultDao(),
        db.badgeDao()
    )

    // Exemplo para observar o histórico de quizzes na tela se necessário
    val historicoQuizzes = repository.observarHistoricoQuizzes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun carregarPerguntas(): List<QuizQuestion> {
        return repository.obterPerguntasQuiz()
    }

    // Chamado quando o utilizador termina o quiz
    fun finalizarQuiz(quizId: Int, acertos: Int, totalPerguntas: Int, onResultadoPronto: (ResultadoQuizModel) -> Unit) {
        viewModelScope.launch {
            val dataHoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val resultado = repository.salvarResultadoQuiz(quizId, acertos, totalPerguntas, dataHoje)
            onResultadoPronto(resultado)
        }
    }
}