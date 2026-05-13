package com.darkk.compatcontrol.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    secondary = AccentDark,
    background = BgDark,
    surface = Surface,
    onPrimary = BgDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = Red
)

@Composable
fun CompatControlTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
