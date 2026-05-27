package com.pokect.bank.kids.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokect.bank.kids.ui.screens.home.components.ConfettiOverlay
import com.pokect.bank.kids.ui.screens.home.components.DepositModal
import com.pokect.bank.kids.ui.screens.home.components.LevelCard
import com.pokect.bank.kids.ui.screens.home.components.MissionSummaryCard
import com.pokect.bank.kids.ui.screens.home.components.PiggyBankCard
import com.pokect.bank.kids.ui.screens.home.components.ResetSection
import com.pokect.bank.kids.ui.theme.PokectBankBackground
import com.pokect.bank.kids.ui.screens.home.components.HomeTopBar
import com.pokect.bank.kids.ui.screens.home.components.TransactionHistoryCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToMissions: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PokectBankBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                HomeTopBar(
                    userName = uiState.userName,
                    level = uiState.level,
                    onLogout = { viewModel.showResetConfirmation() },
                    modifier = Modifier
                )
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE2E8F0),
                thickness = 1.dp
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                PiggyBankCard(
                    balance = uiState.balance,
                    goal = uiState.goal,
                    goalProgressPercent = uiState.getGoalProgressPercent(),
                    depositAmount = uiState.depositAmount,
                    onDepositClick = { viewModel.showDepositModal() },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LevelCard(
                    level = uiState.level,
                    levelTitle = uiState.getLevelTitle(),
                    currentXp = uiState.currentXp,
                    xpToNextLevel = uiState.xpToNextLevel,
                    xpProgressPercent = uiState.getXpProgressPercent(),
                    totalXp = uiState.totalXp,
                    streak = uiState.streak,
                    badges = uiState.badges,
                    selectedAvatar = uiState.selectedAvatar,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                MissionSummaryCard(
                    missions = uiState.missions,
                    onMissionTap = { onNavigateToMissions() },
                    onViewAllTap = { onNavigateToMissions() },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (!uiState.isHistoryExpanded) {

                    Button(
                        onClick = { viewModel.toggleHistory() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEEF2FF),
                            contentColor = Color(0xFF6366F1)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Histórico",
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            Text(
                                text = "Ver Histórico",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                }

                if (uiState.isHistoryExpanded) {

                    Spacer(modifier = Modifier.height(16.dp))

                    TransactionHistoryCard(
                        transactions = uiState.transactions,
                        isExpanded = true,
                        onToggle = { viewModel.toggleHistory() },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }

        if (uiState.showResetConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.hideResetConfirmation() },
                title = {
                    Text(
                        text = "Deseja realizar o logout?",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = "Não se preocupe, você pode voltar a qualquer momento.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmReset(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideResetConfirmation() }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (uiState.resetCompleted) {
            LaunchedEffect(Unit) {
                onNavigateToLogin()
            }
        }

        if (uiState.showConfetti) {
            ConfettiOverlay(
                isActive = true,
                onFinished = { viewModel.onConfettiFinished() }
            )
        }
    }

    if (uiState.showDepositModal) {
        DepositModal(
            depositAmount = uiState.depositAmount,
            isShowingSuccess = uiState.depositSuccess,
            currentBalance = uiState.balance,
            onAmountChange = { viewModel.updateDepositAmount(it) },
            onQuickSelect = { viewModel.selectQuickAmount(it) },
            onIncrement = { viewModel.incrementDepositAmount() },
            onDecrement = { viewModel.decrementDepositAmount() },
            onConfirm = { viewModel.deposit(uiState.depositAmount) },
            onDismiss = { viewModel.hideDepositModal() }
        )
    }
}