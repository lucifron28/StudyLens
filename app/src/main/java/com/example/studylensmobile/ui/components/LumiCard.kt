package com.example.studylensmobile.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LumiCard(
    title: String,
    message: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes imageResId: Int? = null,
    imageContentDescription: String? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    StudyLensCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (imageResId != null) {
                Image(
                    painter = painterResource(imageResId),
                    contentDescription = imageContentDescription,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(104.dp)
                )
            }
            Text(
                text = "Lumi",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (secondaryActionLabel != null && onSecondaryAction != null) {
                    OutlinedButton(
                        onClick = onSecondaryAction,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(secondaryActionLabel)
                    }
                }
                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(primaryActionLabel)
                }
            }
        }
    }
}
