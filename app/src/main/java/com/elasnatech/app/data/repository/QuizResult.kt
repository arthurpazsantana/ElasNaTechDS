package com.elasnatech.app.data.repository

data class QuizResult(
    val id: Long = 0,
    val quizId: Int,
    val score: Int,
    val total: Int,
    val xpGained: Int,
    val date: String
)