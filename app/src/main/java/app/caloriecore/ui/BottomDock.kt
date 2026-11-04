package app.caloriecore.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.navigation.CalorieCoreTab

@Composable
fun BottomDock(
    currentTab: CalorieCoreTab,
    tabLabel: (CalorieCoreTab) -> String,
    onSelect: (CalorieCoreTab) -> Unit
) {
    val largeText = LocalDensity.current.fontScale > 1.3f
    val tabs = CalorieCoreTab.entries.sortedBy { it.navSlot }
    val rows = if (largeText) tabs.chunked(3) else listOf(tabs)

    Column(Modifier.background(MaterialTheme.colorScheme.surface).navigationBarsPadding()) {
        rows.forEach { row ->
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                row.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        onClick = { onSelect(tab) },
                        icon = { DockGlyph(tab = tab, selected = selected) },
                        label = {
                            Text(
                                text = tabLabel(tab),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                maxLines = 2,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    }
}

@Composable
private fun DockGlyph(tab: CalorieCoreTab, selected: Boolean) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Canvas(modifier = Modifier.size(24.dp)) {
        val iconStroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)

        fun dpPoint(x: Float, y: Float) = Offset(x.dp.toPx(), y.dp.toPx())

        fun strokeLine(startX: Float, startY: Float, endX: Float, endY: Float, width: Float = 2f) {
            drawLine(
                color = color,
                start = dpPoint(startX, startY),
                end = dpPoint(endX, endY),
                strokeWidth = width.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        when (tab) {
            CalorieCoreTab.Today -> {
                drawRoundRect(
                    color = color,
                    topLeft = dpPoint(4f, 5f),
                    size = Size(16.dp.toPx(), 15.dp.toPx()),
                    style = iconStroke
                )
                strokeLine(4f, 10f, 20f, 10f)
                strokeLine(8f, 3f, 8f, 7f)
                strokeLine(16f, 3f, 16f, 7f)
            }

            CalorieCoreTab.Food -> {
                strokeLine(8f, 4f, 8f, 20f, 2.1f)
                strokeLine(6f, 4f, 6f, 10f, 1.7f)
                strokeLine(10f, 4f, 10f, 10f, 1.7f)
                strokeLine(15f, 4f, 18f, 20f, 2.1f)
            }

            CalorieCoreTab.Train -> {
                strokeLine(4f, 12f, 20f, 12f, 2.4f)
                strokeLine(6f, 8f, 6f, 16f, 3f)
                strokeLine(18f, 8f, 18f, 16f, 3f)
            }

            CalorieCoreTab.Progress -> {
                strokeLine(4f, 17f, 9f, 12f, 2.3f)
                strokeLine(9f, 12f, 13f, 15f, 2.3f)
                strokeLine(13f, 15f, 20f, 7f, 2.3f)
            }

            CalorieCoreTab.Settings -> {
                drawCircle(color, radius = 6.dp.toPx(), center = dpPoint(12f, 12f), style = iconStroke)
                drawCircle(color, radius = 2.dp.toPx(), center = dpPoint(12f, 12f))
                strokeLine(12f, 2f, 12f, 5f)
                strokeLine(12f, 19f, 12f, 22f)
                strokeLine(2f, 12f, 5f, 12f)
                strokeLine(19f, 12f, 22f, 12f)
            }
        }
    }
}
