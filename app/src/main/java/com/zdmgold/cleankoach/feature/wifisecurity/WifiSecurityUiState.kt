package com.zdmgold.cleankoach.feature.wifisecurity

import com.zdmgold.cleankoach.core.domain.model.WifiSecurityReport

data class WifiSecurityUiState(
    val loading: Boolean = true,
    val report: WifiSecurityReport? = null,
    val error: String? = null
)
