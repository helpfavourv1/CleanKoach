package com.zdmgold.cleankoach.core.ads

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.zdmgold.cleankoach.core.util.ExternalActions

/** Lets a screen report a finished action; pacing and Pro checks live in [AdsManager]. */
class AdActions(
    private val viewModel: AdsViewModel,
    private val activity: Activity?
) {
    fun actionCompleted() {
        activity?.let(viewModel::onActionCompleted)
    }
}

@Composable
fun rememberAdActions(): AdActions {
    val viewModel: AdsViewModel = hiltViewModel()
    val context = LocalContext.current
    return remember(viewModel, context) {
        AdActions(viewModel, with(ExternalActions) { context.findActivity() })
    }
}
