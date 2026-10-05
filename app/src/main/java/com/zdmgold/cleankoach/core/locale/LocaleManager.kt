package com.zdmgold.cleankoach.core.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleManager {

    val supported: List<String> = listOf(
        "en", "es", "pt", "fr", "de", "it", "hi", "in", "ja", "ko",
        "ru", "ar", "tr", "vi", "th", "pl", "nl", "zh-CN", "zh-TW", "bn",
        "uk", "sv", "ro", "cs", "el", "ms", "fa", "ur", "he", "ta",
        "te", "mr", "sw", "hu", "da", "fi", "nb", "sk", "bg", "ca"
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

    /** Maps what Android reports (id, iw, zh-Hans-CN …) to the codes the app stores. */
    private fun normalize(locale: Locale): String = when (val code = locale.language) {
        "id" -> "in"
        "iw" -> "he"
        "no" -> "nb"
        "zh" -> {
            val traditional = locale.script.equals("Hant", ignoreCase = true) ||
                locale.country in setOf("TW", "HK", "MO")
            if (traditional) "zh-TW" else "zh-CN"
        }
        else -> code
    }

    fun current(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return "en"
        val first = locales.get(0) ?: return "en"
        return normalize(first)
    }
}
