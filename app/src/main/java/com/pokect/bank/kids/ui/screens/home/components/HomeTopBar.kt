package com.pokect.bank.kids.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pokect.bank.kids.ui.components.AvatarDisplay
import com.pokect.bank.kids.ui.components.AvatarSize
import com.pokect.bank.kids.ui.theme.PokectBankOnBackground

@Composable
fun HomeTopBar(
    userName: String,
    level: Int,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarDisplay(
                level = level,
                size = AvatarSize.SM,
                showLevel = true
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Olá,",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
                Text(
                    text = if (userName.isNotBlank()) "$userName!" else "Kids!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PokectBankOnBackground
                    )
                )
            }
        }

        IconButton(
            onClick = onLogout,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFEBEE))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = "Sair",
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}