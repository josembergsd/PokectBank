package com.pokect.bank.kids.ui.screens.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokect.bank.kids.ui.theme.KidShapes
import com.pokect.bank.kids.util.formatCurrency

@Composable
fun PiggyBankCard(
    balance: Double,
    goal: Double,
    goalProgressPercent: Float,
    depositAmount: Double,
    onDepositClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = KidShapes.large
            ),
        shape = KidShapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KidShapes.large)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFF7EB3), Color(0xFFFF758C)),
                        start = Offset.Zero,
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🐷",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Meu Cofrinho",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Você tem",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.9f)
                    ),
                )
                Text(
                    text = formatCurrency(balance),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                )

                Spacer(modifier = Modifier.height(20.dp))

                GoalProgressSection(
                    goal = goal,
                    goalProgressPercent = goalProgressPercent
                )

                Spacer(modifier = Modifier.height(2.dp))

                DepositCtaButton(
                    onDepositClick = onDepositClick
                )
            }

            Text(
                text = "✨",
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun GoalProgressSection(
    goal: Double,
    goalProgressPercent: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Progresso da Meta",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f)
            ),
        )
        Text(
            text = "${goalProgressPercent.toInt()}%",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = Color.White
            ),
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    ShinyProgressBar(
        progress = goalProgressPercent / 100f,
        gradient = Brush.horizontalGradient(listOf(Color(0xFF00C853), Color(0xFF69F0AE))),
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "Meta: ${formatCurrency(goal)}",
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = 0.75f)
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(2.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FloatingCoinsRow()
    }
}

@Composable
private fun FloatingCoinsRow() {

    val density = LocalDensity.current

    val floatDistancePx = remember(density) { with(density) { 6.dp.toPx() } }

    val infiniteTransition = rememberInfiniteTransition(label = "floatCoin")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = floatDistancePx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(130.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🪙", fontSize = 15.sp, modifier = Modifier.graphicsLayer { translationY = floatY; alpha = 0.6f })
        Text("🪙", fontSize = 15.sp, modifier = Modifier.graphicsLayer { translationY = -floatY; alpha = 0.8f })
        Text("🪙", fontSize = 15.sp, modifier = Modifier.graphicsLayer { translationY = floatY; alpha = 0.6f })
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
            .background(Color.Black.copy(alpha = 0.15f))
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
                                Color.White.copy(alpha = 0.3f),
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

@Composable
private fun DepositCtaButton(
    onDepositClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = 300f
        ),
        label = "depositButtonScale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .clickable {
                isPressed = true
                onDepositClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "+",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "Guardar Moedinha",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E),
                    fontSize = 15.sp
                ),
            )
        }
    }
}