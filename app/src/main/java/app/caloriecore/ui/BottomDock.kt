package app.caloriecore.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import app.caloriecore.ui.navigation.CalorieCoreTab
import app.caloriecore.ui.theme.LineIcons

@Composable
fun BottomDock(currentTab: CalorieCoreTab, tabLabel: (CalorieCoreTab) -> String, onSelect: (CalorieCoreTab) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        CalorieCoreTab.entries.sortedBy { it.navSlot }.forEach { tab ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon(), contentDescription = null) },
                label = {
                    Text(
                        text = tabLabel(tab),
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

private fun CalorieCoreTab.icon(): ImageVector = when (this) {
    CalorieCoreTab.Today -> LineIcons.Today
    CalorieCoreTab.Food -> LineIcons.Food
    CalorieCoreTab.Train -> LineIcons.Train
    CalorieCoreTab.Progress -> LineIcons.Progress
    CalorieCoreTab.Settings -> Icons.Rounded.Settings
}
