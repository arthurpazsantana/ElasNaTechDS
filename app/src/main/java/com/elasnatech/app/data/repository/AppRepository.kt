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

class AppRepository(
    private val userProfileDao: UserProfileDao,
    private val quizResultDao: QuizResultDao,
    private val badgeDao: BadgeDao
) {

    // --- REGRAS DE NEGÓCIO (Dev 2) ---

    fun calcularXpGanho(acertos: Int): Int {
        return acertos * 10
    }

    fun calcularNivel(xpTotal: Int): Int {
        return (xpTotal / 100) + 1
    }

    fun obterSeloConquistado(pontuacao: Int, totalPerguntas: Int): String {
        return badgeFor(pontuacao, totalPerguntas)
    }

    // --- OPERAÇÕES DO BANCO DE DADOS REAL (ROOM) ---

    // 1. PERFIL DO USUÁRIO
    fun observarPerfil(): Flow<UserProfileEntity?> {
        return userProfileDao.observeProfile()
    }

    suspend fun obterPerfil(): UserProfileEntity? {
        return userProfileDao.getProfile()
    }

    suspend fun salvarPerfil(nome: String, avatarId: Int) {
        val perfilAtual = userProfileDao.getProfile()
        val novoPerfil = UserProfileEntity(
            id = 1,
            name = nome,
            avatarId = avatarId,
            xp = perfilAtual?.xp ?: 0,
            streak = perfilAtual?.streak ?: 0,
            lastActiveDate = perfilAtual?.lastActiveDate ?: ""
        )
        userProfileDao.save(novoPerfil)
    }

    // 2. QUIZ E RESULTADOS
    suspend fun salvarResultadoQuiz(quizId: Int, acertos: Int, totalPerguntas: Int, dataHoje: String) {
        val xpGanho = calcularXpGanho(acertos)

        // Salva o resultado do quiz
        val resultado = QuizResultEntity(
            quizId = quizId,
            score = acertos,
            total = totalPerguntas,
            xpGained = xpGanho,
            date = dataHoje
        )
        quizResultDao.insert(resultado)

        // Adiciona o XP ao perfil do usuário
        userProfileDao.addXp(xpGanho)

        // Checa e salva selo/emblema se o usuário foi bem
        val seloId = obterSeloConquistado(acertos, totalPerguntas)
        if (seloId.isNotEmpty()) {
            badgeDao.insert(EarnedBadgeEntity(badgeId = seloId, earnedDate = dataHoje))
        }
    }

    fun observarHistoricoQuizzes(): Flow<List<QuizResultEntity>> {
        return quizResultDao.observeAll()
    }

    // 3. EMBLEMAS / SELOS
    fun observarEmblemas(): Flow<List<EarnedBadgeEntity>> {
        return badgeDao.observeAll()
    }

    // --- DADOS ESTÁTICOS / PERGUNTAS (MOCKDATA) ---

    fun obterPerguntasQuiz(): List<QuizQuestion> {
        return MockData.quiz
    }

    fun obterCientistas(): List<Scientist> {
        return MockData.scientists
    }
}