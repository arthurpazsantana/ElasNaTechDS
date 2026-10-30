package com.elasnatech.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAOs = as "perguntas" que o app pode fazer ao banco.
 * Funções com Flow avisam a tela sozinhas quando o dado muda.
 * Funções "suspend" precisam ser chamadas de dentro de uma coroutine (viewModelScope).
 */

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfile(): UserProfileEntity?

    // Cria o perfil ou substitui o que já existe
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET xp = xp + :amount WHERE id = 1")
    suspend fun addXp(amount: Int)

    @Query("UPDATE user_profile SET streak = :streak, lastActiveDate = :date WHERE id = 1")
    suspend fun updateStreak(streak: Int, date: String)

    @Query("DELETE FROM user_profile")
    suspend fun clear()
}

@Dao
interface QuizResultDao {
    @Insert
    suspend fun insert(result: QuizResultEntity): Long

    @Query("SELECT * FROM quiz_result ORDER BY id DESC")
    fun observeAll(): Flow<List<QuizResultEntity>>

    // Melhor resultado de um quiz (para mostrar "concluído" e a melhor nota)
    @Query("SELECT * FROM quiz_result WHERE quizId = :quizId ORDER BY score DESC LIMIT 1")
    suspend fun bestForQuiz(quizId: Int): QuizResultEntity?

    @Query("SELECT COUNT(*) FROM quiz_result")
    suspend fun countAll(): Int

    @Query("DELETE FROM quiz_result")
    suspend fun clear()
}

@Dao
interface BadgeDao {
    // IGNORE: se o emblema já foi conquistado, não duplica
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(badge: EarnedBadgeEntity): Long

    @Query("SELECT * FROM earned_badge")
    fun observeAll(): Flow<List<EarnedBadgeEntity>>

    @Query("SELECT COUNT(*) FROM earned_badge WHERE badgeId = :badgeId")
    suspend fun has(badgeId: String): Int

    @Query("DELETE FROM earned_badge")
    suspend fun clear()
}
