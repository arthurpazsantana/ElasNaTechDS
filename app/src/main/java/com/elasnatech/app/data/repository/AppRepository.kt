package com.elasnatech.app.data.repository

import com.elasnatech.app.QuizQuestion
import com.elasnatech.app.Scientist
import com.elasnatech.app.data.db.EarnedBadgeEntity
import com.elasnatech.app.data.db.QuizResultEntity
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun calcularXpGanho(acertos: Int): Int
    fun calcularNivel(xpTotal: Int): Int
    fun obterSeloConquistado(pontuacao: Int, totalPerguntas: Int): String
    fun observarPerfil(): Flow<UserProfile?>
    suspend fun obterPerfil(): UserProfile
    suspend fun salvarPerfil(nome: String, avatarId: Int)
    suspend fun salvarResultadoQuiz(quizId: Int, acertos: Int, totalPerguntas: Int, dataHoje: String): ResultadoQuizModel
    suspend fun obterMelhorResultadoQuiz(quizId: Int): QuizResultEntity?
    fun observarHistoricoQuizzes(): Flow<List<QuizResultEntity>>
    fun observarEmblemas(): Flow<List<EarnedBadgeEntity>>
    fun obterPerguntasQuiz(): List<QuizQuestion>
    fun obterCientistas(): List<Scientist>
}