package com.elasnatech.app.data.repository

data class ResultadoQuizModel(
    val xpGanho: Int,
    val subiuDeNivel: Boolean,
    val novoNivel: Int,
    val emblemasNovos: List<String>
)