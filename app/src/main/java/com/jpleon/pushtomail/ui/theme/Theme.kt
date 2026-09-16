package com.jpleon.pushtomail.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Paleta oscura fija -- la app no sigue el modo claro/oscuro del sistema,
 * siempre se ve igual. Nada de darkColorScheme() dinamico ni isSystemInDarkTheme().
 */
private val PushToMailColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnBackground,
    secondary = Secondary,
    onSecondary = OnSecondary,
    background = Background,
    onBackground = OnBackground,
    surface = SurfaceDark,
    onSurface = OnBackground,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorColor,
    onError = OnErrorColor,
    outline = Outline
)

@Composable
fun PushToMailTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PushToMailColorScheme,
        content = content
    )
}
