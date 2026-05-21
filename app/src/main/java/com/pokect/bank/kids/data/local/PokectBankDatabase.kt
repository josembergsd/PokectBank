package com.pokect.bank.kids.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pokect.bank.kids.data.local.dao.FamilyMemberDao
import com.pokect.bank.kids.data.local.dao.MissionDao
import com.pokect.bank.kids.data.local.dao.RewardDao
import com.pokect.bank.kids.data.local.dao.SavingsGoalDao
import com.pokect.bank.kids.data.local.dao.TransactionDao
import com.pokect.bank.kids.data.local.dao.UserDao
import com.pokect.bank.kids.data.local.dao.WeeklyChallengeDao
import com.pokect.bank.kids.data.models.FamilyMember
import com.pokect.bank.kids.data.models.Mission
import com.pokect.bank.kids.data.models.Reward
import com.pokect.bank.kids.data.models.SavingsGoal
import com.pokect.bank.kids.data.models.Transaction
import com.pokect.bank.kids.data.models.User
import com.pokect.bank.kids.data.models.WeeklyChallenge
import java.time.Duration
import java.time.Instant

@Database(
    entities = [
        User::class,
        Transaction::class,
        SavingsGoal::class,
        Mission::class,
        Reward::class,
        FamilyMember::class,
        WeeklyChallenge::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(PokectBankConverters::class)
abstract class PokectBankDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun missionDao(): MissionDao
    abstract fun rewardDao(): RewardDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun weeklyChallengeDao(): WeeklyChallengeDao

    companion object {
        private const val DB_NAME = "pokectbank.db"

        @Volatile
        private var INSTANCE: PokectBankDatabase? = null

        fun getInstance(context: Context): PokectBankDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PokectBankDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(SeedDatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        fun resetInstance(context: Context) {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
                context.applicationContext.deleteDatabase(DB_NAME)
            }
        }

        private class SeedDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // Seed user — matches AppRepository.kt createSeedUser()
                db.execSQL(
                    """
                    INSERT INTO user (uid, id, name, level, balance, goal, xp, xpToNextLevel, totalXp, streak, coins, badges, selectedAvatar)
                    VALUES (1, 'user-1', 'Lucas', 5, 47.5, 100.0, 750, 1000, 4750, 12, 85, '🏆,⭐,🎯,💰,🔥,📚,🎮,🌟', 1)
                    """.trimIndent()
                )

                // Seed transactions — matches AppRepository.kt createSeedTransactions()
                db.execSQL(
                    """
                    INSERT INTO `transaction` (uid, id, type, title, amount, date, icon)
                    VALUES (1, 't1', 'DEPOSIT', 'Guardou moedinhas', 10.0, '2026-05-13', '💰'),
                           (2, 't2', 'MISSION', 'Missão: Guardar R\$5', 5.0, '2026-05-12', '🎯'),
                           (3, 't3', 'GOAL', 'Meta: Videogame', -20.0, '2026-05-10', '🎮'),
                           (4, 't4', 'REWARD', 'Resgatou: Escudo Mágico', -50.0, '2026-05-08', '🛡️'),
                           (5, 't5', 'DEPOSIT', 'Guardou moedinhas', 15.0, '2026-05-05', '💰')
                    """.trimIndent()
                )

                // Seed savings goals — matches AppRepository.kt createSeedGoals()
                db.execSQL(
                    """
                    INSERT INTO savings_goal (uid, id, title, icon, targetAmount, currentAmount, daysLeft, color)
                    VALUES (1, 'g1', 'Videogame Novo', '🎮', 500.0, 47.5, 45, 'PRIMARY'),
                           (2, 'g2', 'Bicicleta', '🚲', 350.0, 350.0, 0, 'SUCCESS'),
                           (3, 'g3', 'Livro de Aventuras', '📚', 80.0, 47.5, 12, 'SECONDARY')
                    """.trimIndent()
                )

                // Seed missions — matches AppRepository.kt createSeedMissions()
                db.execSQL(
                    """
                    INSERT INTO mission (uid, id, title, description, xp, coins, progress, maxProgress, status, icon, difficulty, category)
                    VALUES (1, 'm1', 'Guardar R\$5 hoje', 'Faça um depósito de pelo menos R\$5', 50, 10, 0, 5, 'AVAILABLE', '🎯', 'EASY', 'DAILY'),
                           (2, 'm2', 'Sequência de 7 dias', 'Mantenha uma streak de 7 dias seguidos economizando', 150, 30, 3, 7, 'IN_PROGRESS', '🔥', 'MEDIUM', 'WEEKLY'),
                           (3, 'm3', 'Meta semanal', 'Atinga 50% da sua meta de economia esta semana', 100, 20, 35, 50, 'IN_PROGRESS', '🎯', 'MEDIUM', 'WEEKLY'),
                           (4, 'm4', 'Super economizador', 'Economize R\$100 no total', 300, 50, 47, 100, 'IN_PROGRESS', '🚀', 'HARD', 'SPECIAL')
                    """.trimIndent()
                )

                // Seed rewards — matches AppRepository.kt createSeedRewards()
                // Boolean values: true = 1, false = 0 (SQLite convention)
                db.execSQL(
                    """
                    INSERT INTO reward (uid, id, title, description, icon, cost, isNew, redeemed)
                    VALUES (1, 'r1', 'Escudo Mágico', 'Protege sua economia por 7 dias', '🛡️', 50, 1, 0),
                           (2, 'r2', 'Power-up 2x', 'Dobra o XP da próxima missão', '⚡', 100, 0, 0),
                           (3, 'r3', 'Dia de Folga', 'Mantém sua streak por 1 dia sem depósito', '🏖️', 75, 1, 0),
                           (4, 'r4', 'Baú de Moedas', 'Ganha 50 moedas extras', '💎', 200, 0, 0)
                    """.trimIndent()
                )

                // Seed family members — matches AppRepository.kt createSeedFamilyMembers()
                // Boolean values: true = 1, false = 0 (SQLite convention)
                db.execSQL(
                    """
                    INSERT INTO family_member (uid, id, name, level, coins, xp, streak, isCurrentUser)
                    VALUES (1, 'fm1', 'Lucas', 5, 85, 4750, 12, 1),
                           (2, 'fm2', 'Sofia', 6, 120, 5800, 18, 0),
                           (3, 'fm3', 'Pedro', 3, 45, 2100, 5, 0),
                           (4, 'fm4', 'Mamãe', 8, 250, 9200, 30, 0),
                           (5, 'fm5', 'Papai', 7, 180, 7500, 22, 0)
                    """.trimIndent()
                )

                // Seed weekly challenge — matches AppRepository.kt createSeedWeeklyChallenges()
                // endDate is computed at creation time (same as AppRepository seed behavior)
                val endDateEpoch = Instant.now().plus(Duration.ofDays(7)).toEpochMilli()
                db.execSQL(
                    """
                    INSERT INTO weekly_challenge (uid, id, title, description, rewardXp, rewardCoins, endDate, participants, hasJoined)
                    VALUES (1, 'wc1', '🏆 Desafio da semana', 'Guarde pelo menos R\$20 esta semana para ganhar recompensas extras', 200, 40, $endDateEpoch, 'fm2,fm4', 0)
                    """.trimIndent()
                )
            }
        }
    }
}
