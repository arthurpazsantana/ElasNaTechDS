package com.elasnatech.app.data.repository

import com.elasnatech.app.QuizQuestion
import com.elasnatech.app.Scientist
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun calcularXpGanho(acertos: Int): Int
    fun calcularNivel(xpTotal: Int): Int
    fun obterSeloConquistado(pontuacao: Int, totalPerguntas: Int): String

    fun observarPerfil(): Flow<UserProfile?>
    suspend fun obterPerfil(): UserProfile
    suspend fun salvarPerfil(nome: String, avatarId: Int)

    suspend fun salvarResultadoQuiz(quizId: Int, acertos: Int, totalPerguntas: Int, dataHoje: String): ResultadoQuizModel
    suspend fun obterMelhorResultadoQuiz(quizId: Int): QuizResult?

    fun observarHistoricoQuizzes(): Flow<List<QuizResult>>
    fun observarEmblemas(): Flow<List<EarnedBadge>>

    fun obterPerguntasQuiz(): List<QuizQuestion>
    fun obterCientistas(): List<Scientist>
}