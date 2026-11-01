package com.elasnatech.app.data.repository

import com.elasnatech.app.QuizQuestion
import com.elasnatech.app.Scientist
import com.elasnatech.app.MockData
import com.elasnatech.app.data.db.EarnedBadgeEntity
import com.elasnatech.app.data.db.QuizResultEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppRepository : AppRepository {
    private val perfilFlow = MutableStateFlow<UserProfile?>(UserProfile(1, "Aluna Fake", 1, 0, 0, ""))
    private val emblemasFlow = MutableStateFlow<List<EarnedBadgeEntity>>(emptyList())
    private val resultadosFlow = MutableStateFlow<List<QuizResultEntity>>(emptyList())

    override fun calcularXpGanho(acertos: Int): Int = acertos * 10
    override fun calcularNivel(xpTotal: Int): Int = (xpTotal / 100) + 1
    override fun obterSeloConquistado(pontuacao: Int, totalPerguntas: Int): String = "Iniciante"

    override fun observarPerfil(): Flow<UserProfile?> = perfilFlow
    override suspend fun obterPerfil(): UserProfile = perfilFlow.value!!
    override suspend fun salvarPerfil(nome: String, avatarId: Int) {
        perfilFlow.value = perfilFlow.value?.copy(name = nome, avatarId = avatarId)
    }
    override suspend fun obterMelhorResultadoQuiz(quizId: Int): QuizResultEntity? {
        return resultadosFlow.value.filter { it.quizId == quizId }.maxByOrNull { it.score }
    }
    override suspend fun salvarResultadoQuiz(quizId: Int, acertos: Int, totalPerguntas: Int, dataHoje: String): ResultadoQuizModel {
        return ResultadoQuizModel(acertos * 10, false, 1, emptyList())
    }
    override fun observarHistoricoQuizzes(): Flow<List<QuizResultEntity>> = resultadosFlow
    override fun observarEmblemas(): Flow<List<EarnedBadgeEntity>> = emblemasFlow
    override fun obterPerguntasQuiz(): List<QuizQuestion> = MockData.quiz
    override fun obterCientistas(): List<Scientist> = MockData.scientists
}