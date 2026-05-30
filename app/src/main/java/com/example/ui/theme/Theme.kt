package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimarySaffron,
    secondary = SecondaryGreen,
    tertiary = HoliPink,
    background = AmbientSkyDark,
    surface = IndianNightCard,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = SoftAmberText,
    onSurface = SoftAmberText
)

private val LightColorScheme = lightColorScheme(
    primary = PrimarySaffron,
    secondary = SecondaryGreen,
    tertiary = AccentMarigold,
    background = Color(0xFFFDF2E9), // Light saffron ambient background
    surface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFF3E2723), // Dark coffee brown text
    onSurface = Color(0xFF3E2723)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to gorgeous nightly festival look
    dynamicColor: Boolean = false, // Keep custom festival branding colors locked
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
