package com.zdmgold.cleankoach.core.analytics

interface Analytics {
    fun event(name: String, params: Map<String, String> = emptyMap())
    fun screen(name: String)
}

object NoOpAnalytics : Analytics {
    override fun event(name: String, params: Map<String, String>) = Unit
    override fun screen(name: String) = Unit
}
