package com.example.taskmaster.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ColorErrorLight = androidx.compose.ui.graphics.Color(0xFFBA1A1A)
private val ColorErrorDark = androidx.compose.ui.graphics.Color(0xFFFFB4AB)

private val DarkColorScheme = darkColorScheme(
    primary = Apricot,
    onPrimary = Night,
    secondary = Slate,
    onSecondary = SoftWhite,
    tertiary = Ember,
    onTertiary = SoftWhite,
    background = Night,
    onBackground = SoftWhite,
    surface = Charcoal,
    onSurface = SoftWhite,
    error = ColorErrorDark,
    onError = SoftWhite
)

private val LightColorScheme = lightColorScheme(
    primary = SunsetCoral,
    onPrimary = SoftWhite,
    secondary = DeepNavy,
    onSecondary = SoftWhite,
    tertiary = Ember,
    onTertiary = SoftWhite,
    background = Sand,
    onBackground = Charcoal,
    surface = SoftWhite,
    onSurface = Charcoal,
    error = ColorErrorLight,
    onError = SoftWhite
)

@Composable
fun TaskMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
