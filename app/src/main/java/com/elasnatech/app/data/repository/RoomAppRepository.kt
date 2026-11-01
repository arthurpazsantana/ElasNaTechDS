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
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RoomAppRepository(
    private val userProfileDao: UserProfileDao,
    private val quizResultDao: QuizResultDao,
    private val badgeDao: BadgeDao
) : AppRepository {

    override fun calcularXpGanho(acertos: Int): Int = acertos * 10

    override fun calcularNivel(xpTotal: Int): Int = (xpTotal / 100) + 1

    override fun obterSeloConquistado(pontuacao: Int, totalPerguntas: Int): String = badgeFor(pontuacao, totalPerguntas)

    // --- Mapeamentos para os Modelos de Domínio ---
    private fun UserProfileEntity.toDomainModel() = UserProfile(
        id = id, name = name, avatarId = avatarId, xp = xp, streak = streak, lastActiveDate = lastActiveDate
    )

    private fun QuizResultEntity.toDomainModel() = QuizResult(
        id = id, quizId = quizId, score = score, total = total, xpGained = xpGained, date = date
    )

    private fun EarnedBadgeEntity.toDomainModel() = EarnedBadge(
        badgeId = badgeId, earnedDate = earnedDate
    )

    override fun observarPerfil(): Flow<UserProfile?> {
        return userProfileDao.observeProfile().map { it?.toDomainModel() }
    }

    override suspend fun obterPerfil(): UserProfile {
        var perfil = userProfileDao.getProfile()
        if (perfil == null) {
            perfil = UserProfileEntity(id = 1, name = "Aluna", avatarId = 1, xp = 0, streak = 0, lastActiveDate = "")
            userProfileDao.save(perfil)
        }
        return perfil.toDomainModel()
    }

    override suspend fun salvarPerfil(nome: String, avatarId: Int) {
        val perfilAtual = obterPerfil()
        val novoPerfil = UserProfileEntity(
            id = perfilAtual.id, name = nome, avatarId = avatarId, xp = perfilAtual.xp, streak = perfilAtual.streak, lastActiveDate = perfilAtual.lastActiveDate
        )
        userProfileDao.save(novoPerfil)
    }

    override suspend fun obterMelhorResultadoQuiz(quizId: Int): QuizResult? {
        return quizResultDao.bestForQuiz(quizId)?.toDomainModel()
    }

    override suspend fun salvarResultadoQuiz(quizId: Int, acertos: Int, totalPerguntas: Int, dataHoje: String): ResultadoQuizModel {
        // Validação exigida pelo Dev 1 (Ponto 5)
        require(totalPerguntas > 0 && acertos in 0..totalPerguntas) { "Valores de quiz inválidos" }

        val xpGanho = calcularXpGanho(acertos)
        val perfilAntigo = obterPerfil()
        val nivelAntigo = calcularNivel(perfilAntigo.xp)

        val resultado = QuizResultEntity(
            quizId = quizId, score = acertos, total = totalPerguntas, xpGained = xpGanho, date = dataHoje
        )
        quizResultDao.insert(resultado)
        userProfileDao.addXp(xpGanho)
        atualizarStreak(dataHoje)

        val perfilAtualizado = obterPerfil()
        val novoNivel = calcularNivel(perfilAtualizado.xp)
        val subiuDeNivel = novoNivel > nivelAntigo
        val emblemasNovos = mutableListOf<String>()

        if (acertos == totalPerguntas) {
            if (badgeDao.has("quiz_100_por_cento") == 0) {
                badgeDao.insert(EarnedBadgeEntity(badgeId = "quiz_100_por_cento", earnedDate = dataHoje))
                emblemasNovos.add("quiz_100_por_cento")
            }
        }
        if (quizResultDao.countAll() == 1) {
            if (badgeDao.has("primeiro_quiz") == 0) {
                badgeDao.insert(EarnedBadgeEntity(badgeId = "primeiro_quiz", earnedDate = dataHoje))
                emblemasNovos.add("primeiro_quiz")
            }
        }
        if (novoNivel >= 5) {
            if (badgeDao.has("nivel_5") == 0) {
                badgeDao.insert(EarnedBadgeEntity(badgeId = "nivel_5", earnedDate = dataHoje))
                emblemasNovos.add("nivel_5")
            }
        }
        if (perfilAtualizado.streak >= 5) {
            if (badgeDao.has("5_dias_seguidos") == 0) {
                badgeDao.insert(EarnedBadgeEntity(badgeId = "5_dias_seguidos", earnedDate = dataHoje))
                emblemasNovos.add("5_dias_seguidos")
            }
        }

        return ResultadoQuizModel(xpGanho, subiuDeNivel, novoNivel, emblemasNovos)
    }

    private suspend fun atualizarStreak(dataHoje: String) {
        val perfil = obterPerfil()
        val ultimaData = perfil.lastActiveDate
        val streakAtual = perfil.streak

        if (ultimaData == dataHoje) return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var novoStreak = 1

        if (ultimaData.isNotEmpty()) {
            try {
                val dateHojeParsed = sdf.parse(dataHoje)
                if (dateHojeParsed != null) {
                    val calOntem = Calendar.getInstance()
                    calOntem.time = dateHojeParsed
                    calOntem.add(Calendar.DAY_OF_YEAR, -1)
                    val dataOntemEsperada = sdf.format(calOntem.time)

                    if (ultimaData == dataOntemEsperada) {
                        novoStreak = streakAtual + 1
                    }
                }
            } catch (e: Exception) {
                novoStreak = 1
            }
        }
        userProfileDao.updateStreak(novoStreak, dataHoje)
    }

    override fun observarHistoricoQuizzes(): Flow<List<QuizResult>> {
        return quizResultDao.observeAll().map { list -> list.map { it.toDomainModel() } }
    }

    override fun observarEmblemas(): Flow<List<EarnedBadge>> {
        return badgeDao.observeAll().map { list -> list.map { it.toDomainModel() } }
    }

    override fun obterPerguntasQuiz(): List<QuizQuestion> = MockData.quiz
    override fun obterCientistas(): List<Scientist> = MockData.scientists
}