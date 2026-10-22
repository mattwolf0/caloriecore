package app.caloriecore.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import app.caloriecore.ui.model.UiThemeMode

private val NightScheme = darkColorScheme(
    primary = GymGreenBright,
    onPrimary = Color(0xFF003920),
    primaryContainer = MossWash,
    onPrimaryContainer = Color(0xFFB9F5D2),
    secondary = FoodAmberBright,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF26372D),
    onSecondaryContainer = Color(0xFFCFE9D9),
    tertiary = CoralBright,
    onTertiary = Color(0xFF5F150D),
    tertiaryContainer = EmberWash,
    onTertiaryContainer = Color(0xFFFFDAD3),
    background = GymNight,
    onBackground = ChalkText,
    surface = GymNight,
    onSurface = ChalkText,
    surfaceVariant = NightHigh,
    onSurfaceVariant = FogText,
    surfaceContainerLowest = Color(0xFF0B0E0C),
    surfaceContainerLow = NightCard,
    surfaceContainer = NightCard,
    surfaceContainerHigh = NightRaised,
    surfaceContainerHighest = NightHigh,
    outline = NightOutline,
    outlineVariant = NightHairline
)

private val PaperScheme = lightColorScheme(
    primary = GymGreen,
    onPrimary = Color.White,
    primaryContainer = MintWash,
    onPrimaryContainer = MintInk,
    secondary = FoodAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDEBE2),
    onSecondaryContainer = Color(0xFF14301F),
    tertiary = WarningRed,
    onTertiary = Color.White,
    tertiaryContainer = CoralWash,
    onTertiaryContainer = CoralInk,
    background = PaperBackground,
    onBackground = InkText,
    surface = PaperBackground,
    onSurface = InkText,
    surfaceVariant = PaperHigh,
    onSurfaceVariant = PencilText,
    surfaceContainerLowest = PaperCard,
    surfaceContainerLow = PaperCard,
    surfaceContainer = PaperCard,
    surfaceContainerHigh = PaperRaised,
    surfaceContainerHighest = PaperHigh,
    outline = PaperOutline,
    outlineVariant = PaperHairline
)

private val OceanScheme = PaperScheme.copy(
    primary = Color(0xFF1F6FB2),
    primaryContainer = Color(0xFFD3E6FA),
    onPrimaryContainer = Color(0xFF001D36),
    secondaryContainer = Color(0xFFDCE7F2),
    onSecondaryContainer = Color(0xFF13293D),
    background = Color(0xFFF3F7FB),
    surface = Color(0xFFF3F7FB),
    surfaceContainerHigh = Color(0xFFE9EFF5),
    surfaceContainerHighest = Color(0xFFDFE7EF)
)

private val SunsetScheme = PaperScheme.copy(
    primary = Color(0xFFC4501E),
    primaryContainer = Color(0xFFFFDBCC),
    onPrimaryContainer = Color(0xFF390C00),
    secondary = Color(0xFF9A6B00),
    secondaryContainer = Color(0xFFF7E3D6),
    onSecondaryContainer = Color(0xFF3B2416),
    tertiary = Color(0xFFB3264C),
    background = Color(0xFFFBF6F2),
    surface = Color(0xFFFBF6F2),
    surfaceContainerHigh = Color(0xFFF4ECE6),
    surfaceContainerHighest = Color(0xFFEDE2DA)
)

private val LavenderScheme = PaperScheme.copy(
    primary = Color(0xFF6B4FB3),
    primaryContainer = Color(0xFFE8DDFF),
    onPrimaryContainer = Color(0xFF22005D),
    secondaryContainer = Color(0xFFE9E3F3),
    onSecondaryContainer = Color(0xFF231B33),
    background = Color(0xFFF7F5FB),
    surface = Color(0xFFF7F5FB),
    surfaceContainerHigh = Color(0xFFEFEBF5),
    surfaceContainerHighest = Color(0xFFE6E1EE)
)

private val BlackScheme = NightScheme.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF121413),
    surfaceContainer = Color(0xFF121413),
    surfaceContainerHigh = Color(0xFF1B1E1C),
    surfaceContainerHighest = Color(0xFF242826)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CalorieCoreTheme(themeMode: UiThemeMode, content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val colors = when (themeMode) {
        UiThemeMode.System -> if (systemDark) NightScheme else PaperScheme
        UiThemeMode.Light -> PaperScheme
        UiThemeMode.Dark -> NightScheme
        UiThemeMode.Ocean -> OceanScheme
        UiThemeMode.Sunset -> SunsetScheme
        UiThemeMode.Lavender -> LavenderScheme
        UiThemeMode.Black -> BlackScheme
    }
    val useNightPalette = colors.background.luminance() < 0.5f
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !useNightPalette
                isAppearanceLightNavigationBars = !useNightPalette
            }
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = CompactGymTypography,
        shapes = AppShapes,
        content = content
    )
}
