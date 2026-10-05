package com.zdmgold.cleankoach.feature.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.cleankoach.R
import com.zdmgold.cleankoach.core.locale.LocaleManager

internal data class Language(@StringRes val labelRes: Int, val code: String)

internal val languages = listOf(
    Language(R.string.language_en, "en"),
    Language(R.string.language_es, "es"),
    Language(R.string.language_pt, "pt"),
    Language(R.string.language_fr, "fr"),
    Language(R.string.language_de, "de"),
    Language(R.string.language_it, "it"),
    Language(R.string.language_hi, "hi"),
    Language(R.string.language_in, "in"),
    Language(R.string.language_ja, "ja"),
    Language(R.string.language_ko, "ko"),
    Language(R.string.language_ru, "ru"),
    Language(R.string.language_ar, "ar"),
    Language(R.string.language_tr, "tr"),
    Language(R.string.language_vi, "vi"),
    Language(R.string.language_th, "th"),
    Language(R.string.language_pl, "pl"),
    Language(R.string.language_nl, "nl"),
    Language(R.string.language_zh_CN, "zh-CN"),
    Language(R.string.language_zh_TW, "zh-TW"),
    Language(R.string.language_bn, "bn"),
    Language(R.string.language_uk, "uk"),
    Language(R.string.language_sv, "sv"),
    Language(R.string.language_ro, "ro"),
    Language(R.string.language_cs, "cs"),
    Language(R.string.language_el, "el"),
    Language(R.string.language_ms, "ms"),
    Language(R.string.language_fa, "fa"),
    Language(R.string.language_ur, "ur"),
    Language(R.string.language_he, "he"),
    Language(R.string.language_ta, "ta"),
    Language(R.string.language_te, "te"),
    Language(R.string.language_mr, "mr"),
    Language(R.string.language_sw, "sw"),
    Language(R.string.language_hu, "hu"),
    Language(R.string.language_da, "da"),
    Language(R.string.language_fi, "fi"),
    Language(R.string.language_nb, "nb"),
    Language(R.string.language_sk, "sk"),
    Language(R.string.language_bg, "bg"),
    Language(R.string.language_ca, "ca")
)

@Composable
fun LanguageScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back)
                    )
                }
                Text(
                    text = stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                LanguageOptions(
                    current = state.language,
                    onSelect = viewModel::setLanguage
                )
            }
        }
    }
}

@Composable
internal fun LanguageOptions(
    current: String,
    onSelect: (String) -> Unit
) {
    SettingsRow(
        label = stringResource(R.string.language_system),
        value = if (current == LocaleManager.SYSTEM) "✓" else null,
        onClick = { onSelect(LocaleManager.SYSTEM) }
    )
    languages.forEach { lang ->
        SettingsRow(
            label = stringResource(lang.labelRes),
            value = if (current == lang.code) "✓" else null,
            onClick = { onSelect(lang.code) }
        )
    }
}
