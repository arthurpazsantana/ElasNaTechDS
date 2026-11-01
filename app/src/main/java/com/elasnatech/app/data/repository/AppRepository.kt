package com.elasnatech.app.data.repository

import com.elasnatech.app.QuizQuestion
import com.elasnatech.app.Scientist
import com.elasnatech.app.badgeFor
import com.elasnatech.app.MockData
import com.elasnatech.app.data.db.UserProfileDao
import com.elasnatech.app.data.db.QuizResultDao
import com.elasnatech.app.data.db.BadgeDao
import com.elasnatech.app.data.db.UserProfileEntity
import com.elasnatech.app.data.db.QuizResultEntity
import com.elasnatech.app.data.db.EarnedBadgeEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Objeto de retorno detalhado para a tela de resultado
data class ResultadoQuizModel(
    val xpGanho: Int,
    val subiuDeNivel: Boolean,
    val novoNivel: Int,
    val emblemasNovos: List<String>
)

class AppRepository(
    private val userProfileDao: UserProfileDao,
    private val quizResultDao: QuizResultDao,
    private val badgeDao: BadgeDao
) {

    // --- REGRAS DE NEGÓCIO ---

    fun calcularXpGanho(acertos: Int): Int {
        return acertos * 10
    }

    fun calcularNivel(xpTotal: Int): Int {
        return (xpTotal / 100) + 1
    }

    fun obterSeloConquistado(pontuacao: Int, totalPerguntas: Int): String {
        return badgeFor(pontuacao, totalPerguntas)
    }

    // --- PERFIL DO USUÁRIO ---

    fun observarPerfil(): Flow<UserProfileEntity?> {
        return userProfileDao.observeProfile()
    }

    suspend fun obterPerfil(): UserProfileEntity {
        var perfil = userProfileDao.getProfile()
        if (perfil == null) {
            perfil = UserProfileEntity(id = 1, name = "Aluna", avatarId = 1, xp = 0, streak = 0, lastActiveDate = "")
            userProfileDao.save(perfil)
        }
        return perfil
    }

    suspend fun salvarPerfil(nome: String, avatarId: Int) {
        val perfilAtual = obterPerfil()
        val novoPerfil = perfilAtual.copy(
            name = nome,
            avatarId = avatarId
        )
        userProfileDao.save(novoPerfil)
    }

    // --- QUIZ E RESULTADOS ---

    suspend fun salvarResultadoQuiz(quizId: Int, acertos: Int, totalPerguntas: Int, dataHoje: String): ResultadoQuizModel {
        val xpGanho = calcularXpGanho(acertos)
        val perfilAntigo = obterPerfil()
        val nivelAntigo = calcularNivel(perfilAntigo.xp)

        // 1. Salva o resultado no banco
        val resultado = QuizResultEntity(
            quizId = quizId,
            score = acertos,
            total = totalPerguntas,
            xpGained = xpGanho,
            date = dataHoje
        )
        quizResultDao.insert(resultado)

        // 2. Atualiza XP com segurança
        userProfileDao.addXp(xpGanho)

        // 3. Atualiza Streak (sequência de dias)
        atualizarStreak(dataHoje)

        // 4. Verificação e atribuição de emblemas pelos IDs corretos
        val emblemasNovos = mutableListOf<String>()

        if (acertos == totalPerguntas) {
            val badgeId = "quiz_100_por_cento"
            if (badgeDao.has(badgeId) == 0) {
                badgeDao.insert(EarnedBadgeEntity(badgeId = badgeId, earnedDate = dataHoje))
                emblemasNovos.add(badgeId)
            }
        }

        if (quizResultDao.countAll() == 1) {
            val badgeId = "primeiro_quiz"
            if (badgeDao.has(badgeId) == 0) {
                badgeDao.insert(EarnedBadgeEntity(badgeId = badgeId, earnedDate = dataHoje))
                emblemasNovos.add(badgeId)
            }
        }

        // Check de novo nível após somar XP
        val perfilAtualizado = obterPerfil()
        val novoNivel = calcularNivel(perfilAtualizado.xp)
        val subiuDeNivel = novoNivel > nivelAntigo

        if (novoNivel >= 5) {
            val badgeId = "nivel_5"
            if (badgeDao.has(badgeId) == 0) {
                badgeDao.insert(EarnedBadgeEntity(badgeId = badgeId, earnedDate = dataHoje))
                emblemasNovos.add(badgeId)
            }
        }

        return ResultadoQuizModel(
            xpGanho = xpGanho,
            subiuDeNivel = subiuDeNivel,
            novoNivel = novoNivel,
            emblemasNovos = emblemasNovos
        )
    }

    fun observarHistoricoQuizzes(): Flow<List<QuizResultEntity>> {
        return quizResultDao.observeAll()
    }

    // --- SEQUÊNCIA DE DIAS (STREAK) ---

    private suspend fun atualizarStreak(dataHoje: String) {
        val perfil = obterPerfil()
        val ultimaData = perfil.lastActiveDate
        val streakAtual = perfil.streak

        if (ultimaData == dataHoje) return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var novoStreak = 1

        if (ultimaData.isNotEmpty()) {
            try {
                val dateUltima = sdf.parse(ultimaData)
                val calOntem = Calendar.getInstance()
                calOntem.add(Calendar.DAY_OF_YEAR, -1)
                val dataOntem = sdf.format(calOntem.time)

                if (ultimaData == dataOntem) {
                    novoStreak = streakAtual + 1
                }
            } catch (e: Exception) {
                novoStreak = 1
            }
        }

        userProfileDao.updateStreak(novoStreak, dataHoje)
    }

    // --- EMBLEMAS ---

    fun observarEmblemas(): Flow<List<EarnedBadgeEntity>> {
        return badgeDao.observeAll()
    }

    // --- MOCK DATA ---

    fun obterPerguntasQuiz(): List<QuizQuestion> {
        return MockData.quiz
    }

    fun obterCientistas(): List<Scientist> {
        return MockData.scientists
    }
}