package com.pokect.bank.kids.ui.screens.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokect.bank.kids.ui.components.AvatarDisplay
import com.pokect.bank.kids.ui.components.AvatarSize
import com.pokect.bank.kids.ui.theme.KidShapes
import com.pokect.bank.kids.ui.theme.PokectBankMutedForeground
import com.pokect.bank.kids.ui.theme.PokectBankOnSurface
import com.pokect.bank.kids.ui.theme.PokectBankSurface

@Composable
fun LevelCard(
    level: Int,
    levelTitle: String,
    currentXp: Int,
    xpToNextLevel: Int,
    xpProgressPercent: Float,
    totalXp: Int,
    streak: Int,
    badges: List<String>,
    selectedAvatar: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = KidShapes.large,
        colors = CardDefaults.cardColors(containerColor = PokectBankSurface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarDisplay(
                    level = level,
                    size = AvatarSize.LG,
                    showLevel = true
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Nível $level",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PokectBankOnSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "✨",
                            fontSize = 20.sp
                        )
                    }
                    Text(
                        text = levelTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PokectBankMutedForeground
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚡",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "$currentXp / $xpToNextLevel XP",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = PokectBankOnSurface
                        )
                    )
                }
                Text(
                    text = "Próximo nível",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Normal,
                        color = PokectBankMutedForeground
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            ShinyProgressBar(
                progress = xpProgressPercent / 100f,
                gradient = Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF818CF8))),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    emoji = "⭐",
                    value = totalXp.toString(),
                    label = "XP Total",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    emoji = "🔥",
                    value = streak.toString(),
                    label = "Dias seguidos",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    emoji = "🏆",
                    value = badges.size.toString(),
                    label = "Conquistas",
                    modifier = Modifier.weight(1f)
                )
            }

            if (badges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Conquistas Recentes",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PokectBankOnSurface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                val maxCircles = 6
                val showOverflow = badges.size > maxCircles
                val visibleBadges = if (showOverflow) badges.take(maxCircles - 1) else badges
                val overflowCount = badges.size - visibleBadges.size

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visibleBadges.forEachIndexed { index, badge ->
                        BadgeChip(
                            emoji = badge,
                            index = index
                        )
                    }
                    if (showOverflow) {
                        OverflowChip(count = overflowCount)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(KidShapes.medium)
            .background(Color(0xFFEEF2FF))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 20.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PokectBankOnSurface
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Normal,
                    color = PokectBankMutedForeground
                ),
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun BadgeChip(
    emoji: String,
    index: Int
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(index) {
        kotlinx.coroutines.delay(index * 100L)
        isVisible = true
    }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        label = "badgeEntrance"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                shadowElevation = 4f,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(Color(0xFFEEF2FF))
            .border(1.dp, Color(0xFFF1F5F9), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun OverflowChip(count: Int) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFFEEF2FF)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$count",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6366F1)
            )
        )
    }
}

@Composable
private fun ShinyProgressBar(
    progress: Float,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shine")
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shineOffset"
    )

    Box(
        modifier = modifier
            .clip(KidShapes.small)
            .background(Color(0xFFE8EAF6))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(KidShapes.small)
                .background(gradient)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = shineOffset * size.width
                    }
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.4f),
                                Color.Transparent,
                            ),
                            startX = 0f,
                            endX = 100f,
                        )
                    )
            )
        }
    }
}
