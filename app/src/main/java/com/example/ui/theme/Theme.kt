package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = FotoFilmGreen,
    onPrimary = Color.White,
    primaryContainer = FotoFilmGreenDark,
    onPrimaryContainer = Color.White,
    secondary = FotoFilmRed,
    onSecondary = Color.White,
    tertiary = FotoFilmRed,
    background = SocialDarkBg,
    surface = SocialDarkSurface,
    surfaceVariant = SocialDarkElevated,
    onBackground = Color(0xFFE4E6EB),
    onSurface = Color(0xFFE4E6EB),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = FotoFilmGreen,
    onPrimary = Color.White,
    primaryContainer = FotoFilmGreenLight,
    onPrimaryContainer = FotoFilmGreenDark,
    secondary = FotoFilmRed,
    onSecondary = Color.White,
    tertiary = FotoFilmRed,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = SocialLightBg,
    onBackground = Color(0xFF050505),
    onSurface = Color(0xFF050505),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
