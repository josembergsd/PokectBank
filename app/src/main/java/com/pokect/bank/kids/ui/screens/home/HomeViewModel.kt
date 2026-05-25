package com.pokect.bank.kids.ui.screens.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.pokect.bank.kids.data.models.MissionStatus
import com.pokect.bank.kids.data.network.ApiErrorResponse
import com.pokect.bank.kids.data.network.UserProfileDto
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
import retrofit2.HttpException

class HomeViewModel(
    private val repository: AppRepository,
    private val homeRepository: HomeRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _apiProfile = MutableStateFlow<UserProfileDto?>(null)

    init {
        // 1. Observa o token e busca o perfil da API
        viewModelScope.launch {
            tokenManager.token
                .distinctUntilChanged()
                .collect { token ->
                    if (!token.isNullOrBlank()) {
                        syncApiProfileToRepository()
                        loadInitialData()
                    }
                }
        }

        // 2. Observa o usuário local (Room) reativamente
        viewModelScope.launch {
            repository.user.collect { user ->
                updateUiWithUser(user)
            }
        }

        // 3. Observa missões e transações locais reativamente
        viewModelScope.launch {
            repository.missions.collect { missions ->
                val inProgress = missions.filter { it.status == MissionStatus.IN_PROGRESS }.take(2)
                _uiState.update { it.copy(missions = inProgress) }
            }
        }

        viewModelScope.launch {
            repository.transactions.collect { transactions ->
                _uiState.update { it.copy(transactions = transactions) }
            }
        }
        
        // 4. Observa o perfil da API e atualiza a UI
        viewModelScope.launch {
            _apiProfile.collect { profile ->
                profile?.let { p ->
                    _uiState.update { it.copy(userName = p.name, selectedAvatar = p.avatarId) }
                }
            }
        }
    }

    private suspend fun syncApiProfileToRepository() {
        if (_apiProfile.value?.name?.isNotBlank() == true) return

        try {
            val profile = homeRepository.getProfile()
            val user = repository.getUser()
            val updatedUser = user.copy(
                name = profile.name,
                selectedAvatar = profile.avatarId
            )
            repository.updateUser(updatedUser)
            _apiProfile.value = profile
        } catch (e: Exception) {
            val errorMessage =
                (e as? HttpException)?.response()?.errorBody()?.string()?.let { json ->
                    try {
                        Gson().fromJson(json, ApiErrorResponse::class.java)
                            .let { it.message ?: it.error }
                    } catch (_: Exception) {
                        null
                    }
                } ?: e.message ?: "Erro inesperado"

            Log.e("HomeViewModel", "Failed to sync profile from API: $errorMessage")
        }
    }

    private suspend fun loadInitialData() {
        if (_uiState.value.userName.isNotBlank()) {
            refreshUserData()
            refreshTransactions()
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        try {
            val user = repository.getUser()
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
                ).withUser(user)
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Erro ao carregar dados: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun updateUiWithUser(user: com.pokect.bank.kids.data.models.User) {
        _uiState.update { state ->
            val profile = _apiProfile.value
            state.copy(
                userName = profile?.name ?: user.name,
                selectedAvatar = profile?.avatarId ?: user.selectedAvatar,
                balance = user.balance,
                goal = user.goal,
                level = user.level,
                currentXp = user.xp,
                xpToNextLevel = user.xpToNextLevel,
                totalXp = user.totalXp,
                streak = user.streak,
                coins = user.coins,
                badges = user.badges
            )
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
                // Centraliza todo o reset no AppContainer de forma síncrona/segura
                com.pokect.bank.kids.di.AppContainer.performFullReset(context)
                
                _uiState.update { it.copy(isResetting = false, resetCompleted = true) }
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
