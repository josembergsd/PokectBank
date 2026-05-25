package com.pokect.bank.kids.ui.screens.missions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokect.bank.kids.data.models.Mission
import com.pokect.bank.kids.data.models.MissionDifficulty
import com.pokect.bank.kids.data.models.MissionStatus
import com.pokect.bank.kids.ui.theme.KidShapes
import com.pokect.bank.kids.ui.theme.PokectBankMutedForeground
import com.pokect.bank.kids.ui.theme.PokectBankOnSurface

@Composable
fun MissionCard(
    mission: Mission,
    modifier: Modifier = Modifier
) {
    val isLocked = mission.status == MissionStatus.LOCKED
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
            modifier = Modifier.weight(1f),
            shape = KidShapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (isLocked) Color(0xFFF8FAFC) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = if (isLocked) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Box(contentAlignment = Alignment.Center) {

                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .then(if (isLocked) Modifier.alpha(0.5f) else Modifier),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isLocked) {
                                    Brush.linearGradient(colors = listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
                                } else {
                                    Brush.linearGradient(colors = listOf(Color(0xFF6366F1), Color(0xFF818CF8)))
                                }
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
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PokectBankOnSurface,
                                        fontSize = 16.sp
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
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            DifficultyChip(difficulty = mission.difficulty, isLocked = isLocked)
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

                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔒", fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DifficultyChip(difficulty: MissionDifficulty, isLocked: Boolean) {
    val chipColor = if (isLocked) Color(0xFFE2E8F0) else when (difficulty) {
        MissionDifficulty.EASY -> Color(0xFFE8F5E9)
        MissionDifficulty.MEDIUM -> Color(0xFFFFF3E0)
        MissionDifficulty.HARD -> Color(0xFFFFEBEE)
    }
    val textColor = if (isLocked) Color.Gray else when (difficulty) {
        MissionDifficulty.EASY -> Color(0xFF2E7D32)
        MissionDifficulty.MEDIUM -> Color(0xFFE65100)
        MissionDifficulty.HARD -> Color(0xFFC62828)
    }
    val difficultyLabel = when (difficulty) {
        MissionDifficulty.EASY -> "Fácil"
        MissionDifficulty.MEDIUM -> "Médio"
        MissionDifficulty.HARD -> "Difícil"
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
