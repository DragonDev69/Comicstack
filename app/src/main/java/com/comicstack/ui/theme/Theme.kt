package com.comicstack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Background = Color(0xFF060608)
val Surface = Color(0xFF101014)
val SurfaceVariant = Color(0xFF1A1A1E)
val Outline = Color(0xFF232328)
val OnBackground = Color(0xFFF0ECE4)
val OnSurface = Color(0xFFCCCCCC)
val OnSurfaceDim = Color(0xFF888888)
val Gold = Color(0xFFE8B84B)
val Green = Color(0xFF27AE60)
val Red = Color(0xFFC0392B)
val BatmanRed = Color(0xFFC0392B)

private val DarkScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Color.Black,
    secondary = Green,
    tertiary = Red,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    outline = Outline,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceDim,
    error = Red
)

@Composable
fun ComicstackTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, content = content)
}
