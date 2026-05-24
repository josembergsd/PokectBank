package com.pokect.bank.kids.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokect.bank.kids.data.models.MissionStatus
import com.pokect.bank.kids.data.repository.AppRepository
import com.pokect.bank.kids.data.repository.RepositoryEvent
import com.pokect.bank.kids.data.repository.home.HomeRepository
import com.pokect.bank.kids.data.session.TokenManager
import com.pokect.bank.kids.ui.theme.AnimationSpecs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.String

class HomeViewModel(
    private val repository: AppRepository,
    private val homeRepository: HomeRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadInitialData() }
        viewModelScope.launch {
            repository.events.distinctUntilChanged().collect { event ->
                when (event) {
                    is RepositoryEvent.UserUpdated -> refreshUserData()
                    is RepositoryEvent.TransactionsChanged -> refreshTransactions()
                    is RepositoryEvent.MissionsChanged,
                    is RepositoryEvent.GoalsChanged,
                    is RepositoryEvent.RewardsChanged,
                    is RepositoryEvent.RankingChanged -> { /* Not relevant to HomeScreen */ }
                }
            }
        }
    }

    private suspend fun loadInitialData() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val profile = homeRepository.getProfile()
            val user = repository.getUser()

            val currentUser = com.pokect.bank.kids.data.models.User(
                id = user.id,
                name = profile.name,
                level = user.level,
                balance = user.balance,
                goal = user.goal,
                xp = user.xp,
                xpToNextLevel = user.xpToNextLevel,
                totalXp = user.totalXp,
                streak = user.streak,
                coins = user.coins,
                badges = user.badges,
                selectedAvatar = profile.avatarId
            )

            val inProgressMissions = repository.getMissions()
                .filter { it.status == MissionStatus.IN_PROGRESS }
                .take(2)
            val transactions = repository.getTransactions()

            _uiState.update {
                it.copy(
                    missions = inProgressMissions,
                    transactions = transactions,
                    isLoading = false,
                    error = null
                ).withUser(currentUser)
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Erro ao carregar dados do perfil: ${e.localizedMessage}"
                )
            }
        }
    }

    private suspend fun refreshUserData() {
        val user = repository.getUser()
        val inProgressMissions = repository.getMissions()
            .filter { it.status == MissionStatus.IN_PROGRESS }
            .take(2)

        _uiState.update {
            it.copy(missions = inProgressMissions).withUser(user)
        }
    }

    private suspend fun refreshTransactions() {
        val transactions = repository.getTransactions()
        _uiState.update { it.copy(transactions = transactions) }
    }

    fun showDepositModal() {
        _uiState.update {
            it.copy(
                showDepositModal = true,
                depositAmount = 5.0,
                depositSuccess = false,
                error = null
            )
        }
    }

    fun hideDepositModal() {
        _uiState.update { it.copy(showDepositModal = false) }
    }

    fun updateDepositAmount(amount: Double) {
        if (amount < 1.0) return
        _uiState.update { it.copy(depositAmount = amount) }
    }

    fun selectQuickAmount(amount: Double) {
        if (amount < 1.0) return
        _uiState.update { it.copy(depositAmount = amount) }
    }

    fun incrementDepositAmount() {
        _uiState.update {
            it.copy(depositAmount = (it.depositAmount + 1.0).coerceAtLeast(1.0))
        }
    }

    fun decrementDepositAmount() {
        _uiState.update {
            it.copy(depositAmount = (it.depositAmount - 1.0).coerceAtLeast(1.0))
        }
    }

    fun deposit(amount: Double) {
        if (amount < 1.0) return

        viewModelScope.launch {
            val result = repository.deposit(amount)
            if (result.isSuccess) {
                // Event collector will handle refresh via UserUpdated
                _uiState.update { it.copy(showDepositModal = false, showConfetti = true) }

                delay(AnimationSpecs.CONFETTI_DURATION.toLong())
                _uiState.update { it.copy(showConfetti = false) }
            } else {
                _uiState.update {
                    it.copy(
                        error = result.exceptionOrNull()?.message ?: "Erro ao guardar moedinhas",
                        showDepositModal = false
                    )
                }
            }
        }
    }

    fun onConfettiFinished() {
        _uiState.update { it.copy(showConfetti = false) }
    }

    fun toggleHistory() {
        _uiState.update { it.copy(isHistoryExpanded = !it.isHistoryExpanded) }
    }

    fun showResetConfirmation() {
        _uiState.update { it.copy(showResetConfirmation = true) }
    }

    fun hideResetConfirmation() {
        _uiState.update { it.copy(showResetConfirmation = false) }
    }

    fun confirmReset(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(showResetConfirmation = false, isResetting = true) }
            try {
                com.pokect.bank.kids.di.AppContainer.loginPreferences.clearLogin()
                tokenManager.clearToken()
                val result = repository.resetData(context)
                
                if (result.isSuccess) {
                    _uiState.update { it.copy(isResetting = false, resetCompleted = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isResetting = false,
                            error = "Erro ao resetar dados. Tente novamente."
                        )
                    }
                }
            } catch (e: CancellationException) {
                _uiState.update { it.copy(isResetting = false) }
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isResetting = false,
                        error = "Erro ao resetar dados. Tente novamente."
                    )
                }
            }
        }
    }

    fun logout(context: Context) {
        confirmReset(context)
    }
}

// ============================================================================
// Top-level helper functions
// ============================================================================

private fun HomeUiState.withUser(user: com.pokect.bank.kids.data.models.User): HomeUiState = copy(
    userName = user.name,
    balance = user.balance,
    goal = user.goal,
    level = user.level,
    currentXp = user.xp,
    xpToNextLevel = user.xpToNextLevel,
    totalXp = user.totalXp,
    streak = user.streak,
    coins = user.coins,
    badges = user.badges,
    selectedAvatar = user.selectedAvatar
)
