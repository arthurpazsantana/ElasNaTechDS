package com.elasnatech.app.data.repository

data class UserProfile(
    val id: Int = 1,
    val name: String,
    val avatarId: Int,
    val xp: Int,
    val streak: Int,
    val lastActiveDate: String
)