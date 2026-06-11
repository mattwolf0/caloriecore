package app.caloriecore.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import app.caloriecore.ui.model.UiThemeMode

private val NightScheme = darkColorScheme(
    primary = GymGreenBright,
    onPrimary = Color(0xFF07120B),
    primaryContainer = MossWash,
    onPrimaryContainer = ChalkText,
    secondary = FoodAmber,
    onSecondary = Color(0xFF1F1404),
    secondaryContainer = ToastWash,
    onSecondaryContainer = ChalkText,
    tertiary = WarningRed,
    background = GymNight,
    onBackground = ChalkText,
    surface = NightCard,
    onSurface = ChalkText,
    surfaceVariant = NightHairline,
    onSurfaceVariant = FogText,
    outline = NightHairline
)

private val PaperScheme = lightColorScheme(
    primary = GymGreen,
    onPrimary = Color.White,
    primaryContainer = MintWash,
    onPrimaryContainer = GymGreen,
    secondary = FoodAmber,
    onSecondary = Color.White,
    secondaryContainer = HoneyWash,
    onSecondaryContainer = InkText,
    tertiary = WarningRed,
    background = PaperWhite,
    onBackground = InkText,
    surface = CardPaper,
    onSurface = InkText,
    surfaceVariant = PaperHairline,
    onSurfaceVariant = PencilText,
    outline = PaperHairline
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
    MaterialTheme(
        colorScheme = if (useNightPalette) NightScheme else PaperScheme,
        typography = CompactGymTypography,
        content = content
    )
}
