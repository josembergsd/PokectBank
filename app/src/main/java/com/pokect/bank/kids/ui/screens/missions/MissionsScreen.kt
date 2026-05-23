package com.pokect.bank.kids.ui.screens.missions

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokect.bank.R
import com.pokect.bank.kids.ui.screens.missions.components.MissionCard
import com.pokect.bank.kids.ui.screens.missions.components.MissionCategoryFilter
import com.pokect.bank.kids.ui.theme.AnimationSpecs
import com.pokect.bank.kids.ui.theme.PokectBankBackground
import com.pokect.bank.kids.ui.theme.PokectBankMutedForeground
import com.pokect.bank.kids.ui.theme.PokectBankOnBackground

@Composable
fun MissionsScreen(
    viewModel: MissionsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PokectBankBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp)
        ) {

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Missões",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PokectBankOnBackground
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Complete missões e ganhe recompensas!",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = PokectBankMutedForeground
                        )
                    )
                }
            }

            item {
                MissionCategoryFilter(
                    selectedFilter = uiState.selectedFilter,
                    onFilterChange = viewModel::selectFilter
                )
            }

            if (uiState.filteredMissions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "🌟",
                                fontSize = 56.sp
                            )
                            Text(
                                text = stringResource(R.string.missions_empty_heading),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PokectBankOnBackground
                                ),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.missions_empty_body),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = PokectBankMutedForeground
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    uiState.filteredMissions,
                    key = { it.id }
                ) { mission ->
                    MissionCard(
                        mission = mission,
                        modifier = Modifier.animateItem(
                            fadeInSpec = AnimationSpecs.screenTransition,
                            fadeOutSpec = tween(200),
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
