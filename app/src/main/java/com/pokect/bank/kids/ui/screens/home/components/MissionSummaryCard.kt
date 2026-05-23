package com.pokect.bank.kids.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokect.bank.kids.data.models.Mission
import com.pokect.bank.kids.data.models.MissionDifficulty
import com.pokect.bank.kids.data.models.MissionStatus
import com.pokect.bank.kids.ui.theme.KidShapes
import com.pokect.bank.kids.ui.theme.PokectBankMutedForeground
import com.pokect.bank.kids.ui.theme.PokectBankOnSurface
import com.pokect.bank.kids.ui.theme.PokectBankPrimary

@Composable
fun MissionSummaryCard(
    missions: List<Mission>,
    onMissionTap: (Mission) -> Unit,
    onViewAllTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Missões Ativas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PokectBankOnSurface
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onViewAllTap() }
            ) {
                Text(
                    text = "Ver todas",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PokectBankPrimary,
                        fontSize = 14.sp
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = PokectBankPrimary,
                    modifier = Modifier.size(16.dp).padding(start = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (missions.isEmpty()) {
            Text(
                text = "Nenhuma missão ativa no momento 😴",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = PokectBankMutedForeground
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                missions.take(2).forEach { mission ->
                    MissionItem(
                        mission = mission,
                        onClick = { onMissionTap(mission) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionItem(
    mission: Mission,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInProgress = mission.status == MissionStatus.IN_PROGRESS
    val progressPercent = if (mission.maxProgress > 0) {
        (mission.progress.toFloat() / mission.maxProgress.toFloat() * 100f).coerceIn(0f, 100f)
    } else {
        0f
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isInProgress) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(50.dp)
                    .background(
                        Color(0xFF6366F1),
                        RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                    )
            )
        }
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() },
            shape = KidShapes.medium,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF818CF8))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = mission.icon, fontSize = 26.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mission.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PokectBankOnSurface
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = mission.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PokectBankMutedForeground,
                                    fontSize = 12.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        val (chipColor, textColor, difficultyLabel) = when (mission.difficulty) {
                            MissionDifficulty.EASY -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Fácil")
                            MissionDifficulty.MEDIUM -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Médio")
                            MissionDifficulty.HARD -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Difícil")
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipColor)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = difficultyLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = textColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    if (isInProgress) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progresso",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF6366F1),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "${mission.progress}/${mission.maxProgress}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PokectBankMutedForeground,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEEF2FF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressPercent / 100f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF6366F1))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⭐", fontSize = 14.sp)
                        Text(
                            text = "${mission.xp} XP",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PokectBankOnSurface,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                        )
                        Text(text = "🪙", fontSize = 14.sp)
                        Text(
                            text = "${mission.coins}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PokectBankOnSurface,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
