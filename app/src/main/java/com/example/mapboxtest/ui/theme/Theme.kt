package com.example.mapboxtest.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)
data class CustomColors(
    val material: ColorScheme,
    val unknotLogo: Color
) {
    val primary: Color get() = material.primary
    val onPrimary: Color get() = material.onPrimary
    val primaryContainer: Color get() = material.primaryContainer
    val onPrimaryContainer: Color get() = material.onPrimaryContainer
    val inversePrimary: Color get() = material.inversePrimary
    val secondary: Color get() = material.secondary
    val onSecondary: Color get() = material.onSecondary
    val secondaryContainer: Color get() = material.secondaryContainer
    val onSecondaryContainer: Color get() = material.onSecondaryContainer
    val tertiary: Color get() = material.tertiary
    val onTertiary: Color get() = material.onTertiary
    val tertiaryContainer: Color get() = material.tertiaryContainer
    val onTertiaryContainer: Color get() = material.onTertiaryContainer
    val background: Color get() = material.background
    val onBackground: Color get() = material.onBackground
    val surface: Color get() = material.surface
    val onSurface: Color get() = material.onSurface
    val surfaceVariant: Color get() = material.surfaceVariant
    val onSurfaceVariant: Color get() = material.onSurfaceVariant
    val surfaceTint: Color get() = material.surfaceTint
    val inverseSurface: Color get() = material.inverseSurface
    val inverseOnSurface: Color get() = material.inverseOnSurface
    val error: Color get() = material.error
    val onError: Color get() = material.onError
    val errorContainer: Color get() = material.errorContainer
    val onErrorContainer: Color get() = material.onErrorContainer
    val outline: Color get() = material.outline
    val outlineVariant: Color get() = material.outlineVariant
    val scrim: Color get() = material.scrim
    val surfaceBright: Color get() = material.surfaceBright
    val surfaceDim: Color get() = material.surfaceDim
    val surfaceContainer: Color get() = material.surfaceContainer
    val surfaceContainerHigh: Color get() = material.surfaceContainerHigh
    val surfaceContainerHighest: Color get() = material.surfaceContainerHighest
    val surfaceContainerLow: Color get() = material.surfaceContainerLow
    val surfaceContainerLowest: Color get() = material.surfaceContainerLowest
}

data class CustomColorsDarkLight(
    val light: CustomColors,
    val dark: CustomColors
)

val AppColors = CustomColorsDarkLight(
    light = CustomColors(
        material = LightColorScheme,
        unknotLogo = Color(0xff282561)
    ),
    dark = CustomColors(
        material = DarkColorScheme,
        unknotLogo = Color(0xFF5853D0)
    )
)

private val LocalColors = staticCompositionLocalOf { AppColors.light }

@Composable
fun MapBoxTestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val appColors = if (darkTheme) AppColors.dark
                    else AppColors.light

    CompositionLocalProvider(
        LocalColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

val MaterialTheme.appColors: CustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current
