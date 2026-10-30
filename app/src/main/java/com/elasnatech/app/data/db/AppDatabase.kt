package com.elasnatech.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Banco de dados do app (arquivo "elas_na_tech.db" dentro do celular).
 *
 * IMPORTANTE: se alguém mudar as tabelas (Entities.kt), aumente o número de `version`
 * e, durante o desenvolvimento, desinstale o app do celular (ou limpe os dados)
 * antes de rodar de novo. Senão o app fecha com erro de "schema".
 */
@Database(
    entities = [
        UserProfileEntity::class,
        QuizResultEntity::class,
        EarnedBadgeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Sempre use esta função. Ela garante que só existe UM banco aberto. */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elas_na_tech.db"
                ).build().also { INSTANCE = it }
            }
    }
}
