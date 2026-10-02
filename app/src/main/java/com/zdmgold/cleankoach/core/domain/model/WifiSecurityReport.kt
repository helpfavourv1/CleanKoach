package com.zdmgold.cleankoach.core.domain.model

enum class SecurityVerdict { SECURE, DETECTED, UNAVAILABLE }

data class WifiSecurityReport(
    val internetAccess: Boolean,
    val encryptionType: String?,
    val sslStripVerdict: SecurityVerdict,
    val sslSplitVerdict: SecurityVerdict,
    val checkedAt: Long
)
