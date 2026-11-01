package com.elasnatech.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabelas do banco. Os nomes terminam em "Entity" para não confundir com os
 * modelos que o Dev 2 vai usar nas telas (UserProfile, QuizResult, Badge).
 */

/** Perfil local do usuário. Só existe UMA linha (id = 1), porque não há login. */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val avatarId: Int,
    val xp: Int = 0,
    val streak: Int = 0,
    val lastActiveDate: String = "" // formato "2026-10-30" (ano-mês-dia)
)

/** Cada vez que alguém termina um quiz, entra uma linha aqui. */
@Entity(tableName = "quiz_result")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Int,
    val score: Int,
    val total: Int,
    val xpGained: Int,
    val date: String // "2026-10-30"
)

/** Emblemas já conquistados. O badgeId é o mesmo id combinado no grupo (ex: "primeiro_quiz"). */
@Entity(tableName = "earned_badge")
data class EarnedBadgeEntity(
    @PrimaryKey val badgeId: String,
    val earnedDate: String // "2026-10-30"
)
