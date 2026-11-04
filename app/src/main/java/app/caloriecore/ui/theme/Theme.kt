package app.caloriecore.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import app.caloriecore.ui.model.UiThemeMode

private val NightScheme = darkColorScheme(
    primary = GymGreenBright,
    onPrimary = Color(0xFF07120B),
    primaryContainer = MossWash,
    onPrimaryContainer = ChalkText,
    secondary = FoodAmber,
    onSecondary = Color(0xFF1F1404),
    secondaryContainer = MossWash,
    onSecondaryContainer = ChalkText,
    tertiary = WarningRed,
    background = GymNight,
    onBackground = ChalkText,
    surface = NightCard,
    onSurface = ChalkText,
    surfaceVariant = NightHairline,
    onSurfaceVariant = FogText,
    outline = Color(0xFF6F7F74)
)

private val PaperScheme = lightColorScheme(
    primary = GymGreen,
    onPrimary = Color.White,
    primaryContainer = MintWash,
    onPrimaryContainer = GymGreen,
    secondary = FoodAmber,
    onSecondary = Color.White,
    secondaryContainer = MintWash,
    onSecondaryContainer = GymGreen,
    tertiary = WarningRed,
    background = PaperWhite,
    onBackground = InkText,
    surface = CardPaper,
    onSurface = InkText,
    surfaceVariant = PaperHairline,
    onSurfaceVariant = PencilText,
    outline = Color(0xFF7D8C82)
)

@Composable
fun CalorieCoreTheme(
    themeMode: UiThemeMode,
    content: @Composable () -> Unit
) {
    val useNightPalette = when (themeMode) {
        UiThemeMode.System -> isSystemInDarkTheme()
        UiThemeMode.Light -> false
        UiThemeMode.Dark -> true
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !useNightPalette
                    isAppearanceLightNavigationBars = !useNightPalette
                }
            }
        }
    }
    MaterialTheme(
        colorScheme = if (useNightPalette) NightScheme else PaperScheme,
        typography = CompactGymTypography,
        shapes = CalorieCoreShapes
    ) {
        ProvideTextStyle(MaterialTheme.typography.bodyMedium, content)
    }
}
