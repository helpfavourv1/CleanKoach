package com.zdmgold.cleankoach.core.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleManager {

    val supported: List<String> = listOf(
        "en", "es", "pt", "fr", "de", "it", "hi", "in", "ja", "ko", "ru"
    )

    fun apply(tag: String) {
        val safe = tag.takeIf { it in supported } ?: "en"
        val list = LocaleListCompat.forLanguageTags(safe)
        AppCompatDelegate.setApplicationLocales(list)
    }

    fun current(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return "en"
        return locales.toLanguageTags().substringBefore(",").substringBefore("-")
    }
}
