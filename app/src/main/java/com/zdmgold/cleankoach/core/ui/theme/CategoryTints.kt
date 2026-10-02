package com.zdmgold.cleankoach.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

data class CategoryTint(
    val container: Color,
    val content: Color
)

@Composable
@ReadOnlyComposable
fun mediaTint(): CategoryTint {
    val dark = isSystemInDarkTheme()
    return if (dark) {
        CategoryTint(CategoryMediaContainerDark, CategoryMediaContentDark)
    } else {
        CategoryTint(CategoryMediaContainerLight, CategoryMediaContentLight)
    }
}

@Composable
@ReadOnlyComposable
fun optimizerTint(): CategoryTint {
    val dark = isSystemInDarkTheme()
    return if (dark) {
        CategoryTint(CategoryOptimizerContainerDark, CategoryOptimizerContentDark)
    } else {
        CategoryTint(CategoryOptimizerContainerLight, CategoryOptimizerContentLight)
    }
}

@Composable
@ReadOnlyComposable
fun toolsTint(): CategoryTint {
    val dark = isSystemInDarkTheme()
    return if (dark) {
        CategoryTint(CategoryToolsContainerDark, CategoryToolsContentDark)
    } else {
        CategoryTint(CategoryToolsContainerLight, CategoryToolsContentLight)
    }
}
