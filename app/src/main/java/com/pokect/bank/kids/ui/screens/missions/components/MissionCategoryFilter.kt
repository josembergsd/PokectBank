package com.pokect.bank.kids.ui.screens.missions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokect.bank.R
import com.pokect.bank.kids.ui.screens.missions.MissionFilterCategory

@Composable
fun MissionCategoryFilter(
    selectedFilter: MissionFilterCategory,
    onFilterChange: (MissionFilterCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = MissionFilterCategory.entries.toTypedArray()
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color(0xFF6366F1) else Color(0xFFEEF2FF))
                    .clickable { onFilterChange(filter) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (filter) {
                        MissionFilterCategory.ALL -> stringResource(R.string.missions_filter_all)
                        MissionFilterCategory.DAILY -> stringResource(R.string.missions_filter_daily)
                        MissionFilterCategory.WEEKLY -> stringResource(R.string.missions_filter_weekly)
                        MissionFilterCategory.SPECIAL -> stringResource(R.string.missions_filter_special)
                    },
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(
                        color = if (isSelected) Color.White else Color(0xFF6366F1),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}
