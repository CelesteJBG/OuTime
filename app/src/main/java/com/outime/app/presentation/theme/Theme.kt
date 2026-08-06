package com.outime.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Esquema de color OuTime — ForestGreen (Light).
 * Único esquema oficial del Design System.
 */
private val OuTimeColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = ForestSecondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = ForestAccent,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = ForestBackground,
    onBackground = OnBackground,
    surface = ForestSurface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = ForestError,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary
)

@Composable
fun OuTimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color desactivado para garantizar una apariencia consistente en todos los dispositivos
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // OuTime usa un único esquema (Light) para mantener la identidad visual del Design System.
    val colorScheme = OuTimeColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = OuTimeShapes,
        content = content
    )
}