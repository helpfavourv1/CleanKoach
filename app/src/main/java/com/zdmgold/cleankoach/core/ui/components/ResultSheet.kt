package com.zdmgold.cleankoach.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zdmgold.cleankoach.R

@Composable
fun ResultSheetContent(
    title: String,
    valueText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = valueText,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.W300,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if (detail != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = stringResource(R.string.action_done),
                onClick = onDismiss
            )
        }
    }
}
