package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color.Black,
    secondary = NeonMagenta,
    onSecondary = Color.White,
    tertiary = HotAmber,
    onTertiary = Color.Black,
    background = DeepSpaceDark,
    onBackground = Color.White,
    surface = SpaceSlate,
    onSurface = Color.White,
    surfaceVariant = DarkGrey,
    onSurfaceVariant = SoftSilver
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
