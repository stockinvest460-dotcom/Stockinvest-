package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    secondary = Blue50,
    tertiary = Teal40,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = OnSurfaceDark,
    onSecondary = OnPrimaryWhite,
    onBackground = Slate300,
    onSurface = Slate300
)

private val LightColorScheme = lightColorScheme(
    primary = Blue60,
    secondary = Blue50,
    tertiary = BlueLight,
    background = SoftBgLight,
    surface = SurfaceWhite,
    onPrimary = OnPrimaryWhite,
    onSecondary = OnPrimaryWhite,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
