package com.elasnatech.app.data.repository

import com.elasnatech.app.QuizQuestion
import com.elasnatech.app.Scientist
import com.elasnatech.app.MockData
import com.elasnatech.app.badgeFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FakeAppRepository : AppRepository {
    private val perfilFlow = MutableStateFlow(UserProfile(1, "Aluna Fake", 1, 0, 0, ""))
    private val emblemasFlow = MutableStateFlow<List<EarnedBadge>>(emptyList())
    private val resultadosFlow = MutableStateFlow<List<QuizResult>>(emptyList())
    private var idCounter = 1L

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
        val perfilAntigo = perfilFlow.value
        val nivelAntigo = calcularNivel(perfilAntigo.xp)
        val novoXp = perfilAntigo.xp + xpGanho

        // 1. Simular salvamento de resultado com ID único
        val novoResultado = QuizResult(
            id = idCounter++,
            quizId = quizId,
            score = acertos,
            total = totalPerguntas,
            xpGained = xpGanho,
            date = dataHoje
        )
        resultadosFlow.update { it + novoResultado }

        // 2. Simular atualização de streak
        val ultimaData = perfilAntigo.lastActiveDate
        var novoStreak = perfilAntigo.streak
        if (ultimaData != dataHoje) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            if (ultimaData.isEmpty()) {
                novoStreak = 1
            } else {
                try {
                    val dateHojeParsed = sdf.parse(dataHoje)
                    if (dateHojeParsed != null) {
                        val calOntem = Calendar.getInstance()
                        calOntem.time = dateHojeParsed
                        calOntem.add(Calendar.DAY_OF_YEAR, -1)
                        val dataOntemEsperada = sdf.format(calOntem.time)
                        if (ultimaData == dataOntemEsperada) {
                            novoStreak += 1
                        } else {
                            novoStreak = 1
                        }
                    }
                } catch (e: Exception) {
                    novoStreak = 1
                }
            }
        }

        perfilFlow.update { it.copy(xp = novoXp, streak = novoStreak, lastActiveDate = dataHoje) }

        val perfilAtualizado = perfilFlow.value
        val novoNivel = calcularNivel(perfilAtualizado.xp)
        val subiuDeNivel = novoNivel > nivelAntigo
        val novosEmblemas = mutableListOf<String>()

        // 3. Verificação de emblemas simulada
        if (acertos == totalPerguntas) {
            val badgeId = "quiz_100_por_cento"
            if (emblemasFlow.value.none { it.badgeId == badgeId }) {
                emblemasFlow.update { it + EarnedBadge(badgeId, dataHoje) }
                novosEmblemas.add(badgeId)
            }
        }
        if (resultadosFlow.value.size == 1) {
            val badgeId = "primeiro_quiz"
            if (emblemasFlow.value.none { it.badgeId == badgeId }) {
                emblemasFlow.update { it + EarnedBadge(badgeId, dataHoje) }
                novosEmblemas.add(badgeId)
            }
        }
        if (novoNivel >= 5) {
            val badgeId = "nivel_5"
            if (emblemasFlow.value.none { it.badgeId == badgeId }) {
                emblemasFlow.update { it + EarnedBadge(badgeId, dataHoje) }
                novosEmblemas.add(badgeId)
            }
        }
        if (novoStreak >= 5) {
            val badgeId = "5_dias_seguidos"
            if (emblemasFlow.value.none { it.badgeId == badgeId }) {
                emblemasFlow.update { it + EarnedBadge(badgeId, dataHoje) }
                novosEmblemas.add(badgeId)
            }
        }

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