package com.elasnatech.app.viewmodel

import androidx.lifecycle.ViewModel
import com.elasnatech.app.QuizQuestion
import com.elasnatech.app.data.repository.AppRepository

class QuizViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    fun obterPerguntas(): List<QuizQuestion> {
        return repository.obterPerguntasQuiz()
    }

    fun calcularResultado(acertos: Int, totalPerguntas: Int): Pair<Int, String> {
        val xpGanho = repository.calcularXpGanho(acertos)
        val selo = repository.obterSeloConquistado(acertos, totalPerguntas)
        return Pair(xpGanho, selo)
    }
}