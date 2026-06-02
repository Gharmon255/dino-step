package com.gharmon255.dinostep.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = DinoLeafGreen,
    onPrimary = Color.White,
    primaryContainer = DinoLeafGreenLight,
    onPrimaryContainer = DinoForestDark,
    secondary = DinoAmber,
    onSecondary = Color.White,
    secondaryContainer = DinoAmberLight,
    onSecondaryContainer = DinoAmberDark,
    tertiary = DinoEarth,
    onTertiary = Color.White,
    background = DinoCreamBackground,
    onBackground = DinoTextDark,
    surface = DinoParchmentSurface,
    onSurface = DinoTextDark,
    surfaceVariant = DinoStoneVariant,
    onSurfaceVariant = DinoTextMuted,
    outline = DinoOutline,
    outlineVariant = DinoStoneVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = DinoLeafGreenBright,
    onPrimary = DinoLeafGreenDark,
    primaryContainer = DinoForestDark,
    onPrimaryContainer = DinoLeafGreenLight,
    secondary = DinoAmberBright,
    onSecondary = DinoAmberOnDark,
    secondaryContainer = DinoAmberDark,
    onSecondaryContainer = DinoAmberLight,
    tertiary = DinoEarth,
    onTertiary = DinoTextLight,
    background = DinoNightBackground,
    onBackground = DinoTextLight,
    surface = DinoMossSurface,
    onSurface = DinoTextLight,
    surfaceVariant = DinoMossVariant,
    onSurfaceVariant = DinoTextMutedDark,
    outline = DinoTextMutedDark,
    outlineVariant = DinoMossVariant,
)

@Composable
fun DinoStepTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** Off by default so Dino Step brand colors apply instead of system Material You purple. */
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
