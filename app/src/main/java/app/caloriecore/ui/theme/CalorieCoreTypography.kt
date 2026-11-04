package app.caloriecore.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val bodyText = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontSize = 14.sp,
    lineHeight = 20.sp
)
private val smallText = bodyText.copy(fontSize = 12.sp, lineHeight = 16.sp)
private val cardTitle = bodyText.copy(
    fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold
)
private val metricText = bodyText.copy(
    fontSize = 24.sp, lineHeight = 32.sp,
    fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"
)

val CompactGymTypography = Typography(
    displayMedium = metricText.copy(fontSize = 32.sp, lineHeight = 40.sp),
    displaySmall = metricText,
    headlineMedium = metricText.copy(fontWeight = FontWeight.Bold),
    headlineSmall = metricText,
    titleLarge = metricText,
    titleMedium = cardTitle,
    titleSmall = bodyText.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = bodyText.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = bodyText,
    bodySmall = smallText,
    labelLarge = bodyText.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = smallText.copy(fontWeight = FontWeight.Medium),
    labelSmall = smallText
)
