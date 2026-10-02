package com.zdmgold.cleankoach.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PermissionGate(
    granted: Boolean,
    modifier: Modifier = Modifier,
    deniedContent: @Composable () -> Unit,
    grantedContent: @Composable () -> Unit
) {
    if (granted) {
        grantedContent()
    } else {
        deniedContent()
    }
}
