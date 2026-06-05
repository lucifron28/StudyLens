package com.example.modulelensmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.modulelensmobile.ui.theme.ModuleTeal

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ModuleTeal.copy(alpha = 0.1f),
    textColor: Color = ModuleTeal
) {
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
