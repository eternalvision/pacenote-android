package com.alexander.pacenote.presentation.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val MidnightColors = darkColorScheme(
    primary = PaceLime,
    onPrimary = Color(0xFF0B0B0C),
    primaryContainer = PaceLimeContainer,
    onPrimaryContainer = PaceLimeSoft,
    secondary = LocalAccent,
    onSecondary = Color(0xFF0B0B0C),
    secondaryContainer = LocalContainer,
    onSecondaryContainer = Color(0xFF0B0B0C),
    tertiary = RemoteAccent,
    onTertiary = Color(0xFF0B0B0C),
    tertiaryContainer = RemoteContainer,
    onTertiaryContainer = Frost,
    background = Midnight,
    onBackground = Frost,
    surface = MidnightRaised,
    onSurface = Frost,
    surfaceVariant = GraphiteHigh,
    onSurfaceVariant = Steel,
    outline = Color(0xFF34343A),
    outlineVariant = Hairline,
    error = Danger,
    onError = Color(0xFF2C0007),
    errorContainer = DangerContainer,
    onErrorContainer = Color(0xFFFFD9DD),
    inverseSurface = Frost,
    inverseOnSurface = Graphite,
    inversePrimary = Color(0xFF303034),
    scrim = Color.Black,
)

private val PaceNoteShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun PaceNoteTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (
        dynamicColor && darkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    ) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        MidnightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PaceNoteTypography,
        shapes = PaceNoteShapes,
        content = content,
    )
}
