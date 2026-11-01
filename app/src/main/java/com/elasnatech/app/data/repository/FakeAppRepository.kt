package com.elasnatech.app.data.repository

import com.elasnatech.app.QuizQuestion
import com.elasnatech.app.Scientist
import com.elasnatech.app.MockData
import com.elasnatech.app.badgeFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeAppRepository : AppRepository {
    private val perfilFlow = MutableStateFlow(UserProfile(1, "Aluna Fake", 1, 0, 0, ""))
    private val emblemasFlow = MutableStateFlow<List<EarnedBadge>>(emptyList())
    private val resultadosFlow = MutableStateFlow<List<QuizResult>>(emptyList())

    override fun calcularXpGanho(acertos: Int): Int = acertos * 10
    override fun calcularNivel(xpTotal: Int): Int = (xpTotal / 100) + 1
    override fun obterSeloConquistado(pontuacao: Int, totalPerguntas: Int): String = badgeFor(pontuacao, totalPerguntas)

    override fun observarPerfil(): Flow<UserProfile?> = perfilFlow
    override suspend fun obterPerfil(): UserProfile = perfilFlow.value
    override suspend fun salvarPerfil(nome: String, avatarId: Int) {
        perfilFlow.update { it.copy(name = nome, avatarId = avatarId) }
    }

    override suspend fun obterMelhorResultadoQuiz(quizId: Int): QuizResult? {
        return resultadosFlow.value.filter { it.quizId == quizId }.maxByOrNull { it.score }
    }

    override suspend fun salvarResultadoQuiz(quizId: Int, acertos: Int, totalPerguntas: Int, dataHoje: String): ResultadoQuizModel {
        require(totalPerguntas > 0 && acertos in 0..totalPerguntas) { "Valores de quiz inválidos" }

        val xpGanho = calcularXpGanho(acertos)
        val novoXp = perfilFlow.value.xp + xpGanho
        val nivelAntigo = calcularNivel(perfilFlow.value.xp)
        val novoNivel = calcularNivel(novoXp)
        val subiuDeNivel = novoNivel > nivelAntigo

        val novoResultado = QuizResult(
            quizId = quizId,
            score = acertos,
            total = totalPerguntas,
            xpGained = xpGanho,
            date = dataHoje
        )
        resultadosFlow.update { it + novoResultado }

        val novosEmblemas = mutableListOf<String>()
        if (acertos == totalPerguntas) {
            val badgeId = "quiz_100_por_cento"
            if (emblemasFlow.value.none { it.badgeId == badgeId }) {
                emblemasFlow.update { it + EarnedBadge(badgeId, dataHoje) }
                novosEmblemas.add(badgeId)
            }
        }

        perfilFlow.update { it.copy(xp = novoXp) }

        return ResultadoQuizModel(
            xpGanho = xpGanho,
            subiuDeNivel = subiuDeNivel,
            novoNivel = novoNivel,
            emblemasNovos = novosEmblemas
        )
    }

    override fun observarHistoricoQuizzes(): Flow<List<QuizResult>> = resultadosFlow
    override fun observarEmblemas(): Flow<List<EarnedBadge>> = emblemasFlow
    override fun obterPerguntasQuiz(): List<QuizQuestion> = MockData.quiz
    override fun obterCientistas(): List<Scientist> = MockData.scientists
}