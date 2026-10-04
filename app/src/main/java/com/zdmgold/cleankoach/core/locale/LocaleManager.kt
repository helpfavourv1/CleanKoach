package com.zdmgold.cleankoach.core.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleManager {

    val supported: List<String> = listOf(
        "en", "es", "pt", "fr", "de", "it", "hi", "in", "ja", "ko", "ru"
    )

    const val SYSTEM = "system"

    fun apply(tag: String) {
        if (tag == SYSTEM) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            return
        }
        val safe = tag.takeIf { it in supported } ?: "en"
        val list = LocaleListCompat.forLanguageTags(safe)
        AppCompatDelegate.setApplicationLocales(list)
    }

    /** Android reports modern codes for a few languages; the app stores the legacy ones. */
    private fun normalize(code: String): String = when (code) {
        "id" -> "in"
        else -> code
    }

    fun current(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return "en"
        return normalize(locales.toLanguageTags().substringBefore(",").substringBefore("-"))
    }
}
