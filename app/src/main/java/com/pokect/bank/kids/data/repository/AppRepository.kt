package com.pokect.bank.kids.data.repository

import android.content.Context
import android.util.Log
import com.pokect.bank.kids.data.local.PokectBankDatabase
import com.pokect.bank.kids.data.local.dao.FamilyMemberDao
import com.pokect.bank.kids.data.local.dao.MissionDao
import com.pokect.bank.kids.data.local.dao.RewardDao
import com.pokect.bank.kids.data.local.dao.SavingsGoalDao
import com.pokect.bank.kids.data.local.dao.TransactionDao
import com.pokect.bank.kids.data.local.dao.UserDao
import com.pokect.bank.kids.data.local.dao.WeeklyChallengeDao
import com.pokect.bank.kids.data.models.FamilyMember
import com.pokect.bank.kids.data.models.GoalColor
import com.pokect.bank.kids.data.models.Mission
import com.pokect.bank.kids.data.models.MissionCategory
import com.pokect.bank.kids.data.models.MissionDifficulty
import com.pokect.bank.kids.data.models.MissionStatus
import com.pokect.bank.kids.data.models.Reward
import com.pokect.bank.kids.data.models.SavingsGoal
import com.pokect.bank.kids.data.models.Transaction
import com.pokect.bank.kids.data.models.TransactionType
import com.pokect.bank.kids.data.models.User
import com.pokect.bank.kids.data.models.WeeklyChallenge
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.min

class AppRepository(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val missionDao: MissionDao,
    private val rewardDao: RewardDao,
    private val familyMemberDao: FamilyMemberDao,
    private val weeklyChallengeDao: WeeklyChallengeDao,
    private val database: PokectBankDatabase
) {

    // === Repository coroutine scope ===
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _isShutdown = java.util.concurrent.atomic.AtomicBoolean(false)

    // === StateFlows (bridged from Room DAO Flows) ===
    private val _user = MutableStateFlow(
        User(
            name = "", level = 1, balance = 0.0, goal = 0.0,
            xp = 0, xpToNextLevel = 1000, totalXp = 0,
            streak = 0, coins = 0,
            badges = emptyList(), selectedAvatar = 1
        )
    )
    val user: StateFlow<User> = _user.asStateFlow()

    private val _missions = MutableStateFlow<List<Mission>>(emptyList())
    val missions: StateFlow<List<Mission>> = _missions.asStateFlow()

    private val _goals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val goals: StateFlow<List<SavingsGoal>> = _goals.asStateFlow()

    private val _rewards = MutableStateFlow<List<Reward>>(emptyList())
    val rewards: StateFlow<List<Reward>> = _rewards.asStateFlow()

    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _weeklyChallenges = MutableStateFlow<List<WeeklyChallenge>>(emptyList())
    val weeklyChallenges: StateFlow<List<WeeklyChallenge>> = _weeklyChallenges.asStateFlow()

    // === Event broadcast (unchanged) ===
    private val _events = MutableSharedFlow<RepositoryEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<RepositoryEvent> = _events.asSharedFlow()

    private fun broadcastEvent(event: RepositoryEvent) {
        if (!_events.tryEmit(event)) {
            Log.w("AppRepository", "Dropped $event — buffer full")
        }
    }

    init {
        // Bridge: DAO Flow → StateFlow — one launch per entity
        // catch {} ensures unhandled exceptions don't propagate (e.g., during testing)
        repositoryScope.launch {
            userDao.observeUser()
                .catch { Log.w("AppRepository", "User Flow error", it) }
                .collect { _user.value = it }
        }
        repositoryScope.launch {
            transactionDao.observeTransactions()
                .catch { Log.w("AppRepository", "Transactions Flow error", it) }
                .collect { _transactions.value = it }
        }
        repositoryScope.launch {
            savingsGoalDao.observeGoals()
                .catch { Log.w("AppRepository", "Goals Flow error", it) }
                .collect { _goals.value = it }
        }
        repositoryScope.launch {
            missionDao.observeMissions()
                .catch { Log.w("AppRepository", "Missions Flow error", it) }
                .collect { _missions.value = it }
        }
        repositoryScope.launch {
            rewardDao.observeRewards()
                .catch { Log.w("AppRepository", "Rewards Flow error", it) }
                .collect { _rewards.value = it }
        }
        repositoryScope.launch {
            familyMemberDao.observeFamilyMembers()
                .catch { Log.w("AppRepository", "FamilyMembers Flow error", it) }
                .collect { _familyMembers.value = it }
        }
        repositoryScope.launch {
            weeklyChallengeDao.observeChallenges()
                .catch { Log.w("AppRepository", "WeeklyChallenges Flow error", it) }
                .collect { _weeklyChallenges.value = it }
        }
    }

    // === Read operations ===
    suspend fun getUser(): User = userDao.getUser()
    suspend fun getMissions(): List<Mission> = missionDao.observeMissions().first()
    suspend fun getGoals(): List<SavingsGoal> = savingsGoalDao.getGoals()
    suspend fun getRewards(): List<Reward> = rewardDao.getRewards()
    suspend fun getFamilyMembers(): List<FamilyMember> = familyMemberDao.getFamilyMembers()
    suspend fun getTransactions(): List<Transaction> = transactionDao.observeTransactions().first()
    suspend fun getWeeklyChallenges(): List<WeeklyChallenge> = weeklyChallengeDao.getChallenges()

    // === Write operations ===

    suspend fun deposit(amount: Double): Result<Unit> {
        if (_isShutdown.get()) return Result.failure(IllegalStateException("Repositório finalizado"))
        if (amount < 1.0) return Result.failure(IllegalArgumentException("Deposit amount must be at least 1.0"))
        if (amount > 10000.0) return Result.failure(IllegalArgumentException("Deposit amount exceeds maximum"))

        return try {
            val currentUser = userDao.getUser()
            val updatedUser = currentUser.copy(
                balance = currentUser.balance + amount,
                coins = currentUser.coins + amount.toInt(),
                xp = currentUser.xp + (amount * 10).toInt(),
                totalXp = currentUser.totalXp + (amount * 10).toInt()
            )
            userDao.updateUser(updatedUser)
            familyMemberDao.syncCurrentUser(
                xp = updatedUser.totalXp,
                coins = updatedUser.coins,
                level = updatedUser.level
            )
            transactionDao.insertTransaction(
                Transaction(
                    id = "t_${java.util.UUID.randomUUID()}",
                    type = TransactionType.DEPOSIT,
                    title = "Guardou moedinhas",
                    amount = amount,
                    date = java.time.LocalDate.now().toString(),
                    icon = "💰"
                )
            )
            broadcastEvent(RepositoryEvent.UserUpdated)
            broadcastEvent(RepositoryEvent.TransactionsChanged)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGoal(
        title: String,
        icon: String,
        targetAmount: Double,
        daysLeft: Int
    ): Result<Unit> {
        if (_isShutdown.get()) return Result.failure(IllegalStateException("Repositório finalizado"))
        return try {
            if (title.isBlank()) return Result.failure(IllegalArgumentException("Title cannot be empty"))
            if (icon.isBlank()) return Result.failure(IllegalArgumentException("Icon cannot be empty"))
            if (targetAmount <= 0.0) return Result.failure(IllegalArgumentException("Target amount must be positive"))
            if (daysLeft <= 0) return Result.failure(IllegalArgumentException("Days left must be positive"))

            val currentGoals = savingsGoalDao.getGoals()
            val newGoal = SavingsGoal(
                id = "g_${java.util.UUID.randomUUID()}",
                title = title.trim(),
                icon = icon,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                daysLeft = daysLeft,
                color = GoalColor.entries[currentGoals.size % GoalColor.entries.size]
            )
            savingsGoalDao.insertGoal(newGoal)
            broadcastEvent(RepositoryEvent.GoalsChanged)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun redeemReward(rewardId: String): Result<Unit> {
        if (_isShutdown.get()) return Result.failure(IllegalStateException("Repositório finalizado"))
        val reward = rewardDao.getRewards().find { it.id == rewardId }
            ?: return Result.failure(IllegalArgumentException("Reward not found: $rewardId"))

        return try {
            val currentUser = userDao.getUser()
            if (currentUser.coins < reward.cost) {
                return Result.failure(IllegalStateException("Insufficient coins"))
            }
            val updatedUser = currentUser.copy(coins = currentUser.coins - reward.cost)
            userDao.updateUser(updatedUser)
            rewardDao.updateReward(reward.copy(redeemed = true))
            familyMemberDao.syncCurrentUser(
                xp = updatedUser.totalXp,
                coins = updatedUser.coins,
                level = updatedUser.level
            )
            transactionDao.insertTransaction(
                Transaction(
                    id = "t_${java.util.UUID.randomUUID()}",
                    type = TransactionType.REWARD,
                    title = "Resgatou: ${reward.title}",
                    amount = (-reward.cost).toDouble(),
                    date = java.time.LocalDate.now().toString(),
                    icon = reward.icon
                )
            )
            broadcastEvent(RepositoryEvent.UserUpdated)
            broadcastEvent(RepositoryEvent.RewardsChanged)
            broadcastEvent(RepositoryEvent.TransactionsChanged)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun allocateToGoal(goalId: String, amount: Double): Result<Unit> {
        if (_isShutdown.get()) return Result.failure(IllegalStateException("Repositório finalizado"))
        if (amount <= 0) return Result.failure(IllegalArgumentException("Amount must be greater than 0"))

        return try {
            val currentGoals = savingsGoalDao.getGoals()
            val goal = currentGoals.find { it.id == goalId }
                ?: return Result.failure(IllegalArgumentException("Goal not found: $goalId"))

            val remainingAmount = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
            if (remainingAmount <= 0.0) {
                return Result.failure(IllegalStateException("Goal already completed"))
            }
            val amountToAllocate = min(amount, remainingAmount)

            val user = userDao.getUser()
            if (user.balance < amountToAllocate) {
                return Result.failure(IllegalStateException("Insufficient balance"))
            }

            userDao.updateUser(user.copy(balance = user.balance - amountToAllocate))
            savingsGoalDao.updateGoal(goal.copy(currentAmount = goal.currentAmount + amountToAllocate))
            transactionDao.insertTransaction(
                Transaction(
                    id = "t_${java.util.UUID.randomUUID()}",
                    type = TransactionType.GOAL,
                    title = "Alocação: ${goal.title}",
                    amount = -amountToAllocate,
                    date = java.time.LocalDate.now().toString(),
                    icon = goal.icon
                )
            )

            broadcastEvent(RepositoryEvent.GoalsChanged)
            broadcastEvent(RepositoryEvent.UserUpdated)
            broadcastEvent(RepositoryEvent.TransactionsChanged)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetData(context: Context): Result<Unit> {
        _isShutdown.set(true)
        return try {
            repositoryScope.cancel()
            PokectBankDatabase.resetInstance(context)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinWeeklyChallenge(challengeId: String): Result<Unit> {
        if (_isShutdown.get()) return Result.failure(IllegalStateException("Repositório finalizado"))
        return try {
            val currentFamilyMembers = familyMemberDao.getFamilyMembers()
            val currentUserId = currentFamilyMembers.find { it.isCurrentUser }?.id
                ?: return Result.failure(IllegalStateException("No current user found in family members"))

            val challenges = weeklyChallengeDao.getChallenges()
            val targetChallenge = challenges.find { it.id == challengeId }
                ?: return Result.failure(IllegalArgumentException("Challenge not found: $challengeId"))

            if (targetChallenge.hasJoined) {
                return Result.failure(IllegalStateException("Already joined this challenge"))
            }

            val newParticipants = if (targetChallenge.participants.contains(currentUserId)) {
                targetChallenge.participants
            } else {
                targetChallenge.participants + currentUserId
            }
            weeklyChallengeDao.updateChallenge(
                targetChallenge.copy(hasJoined = true, participants = newParticipants)
            )

            val newMission = Mission(
                id = "m_challenge_${challengeId}",
                title = "Desafio: ${targetChallenge.title}",
                description = targetChallenge.description,
                xp = targetChallenge.rewardXp,
                coins = targetChallenge.rewardCoins,
                progress = 0,
                maxProgress = 1,
                status = MissionStatus.IN_PROGRESS,
                icon = "⚡",
                difficulty = MissionDifficulty.MEDIUM,
                category = MissionCategory.SPECIAL
            )
            missionDao.insertMission(newMission)

            broadcastEvent(RepositoryEvent.MissionsChanged)
            broadcastEvent(RepositoryEvent.RankingChanged)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
