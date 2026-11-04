package app.caloriecore.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing

@Composable
internal fun LegalInfoScreen(strings: CalorieCoreStrings, onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val notices = legalNotices(strings)

    BackHandler(onBack = onBack)

    LazyColumn(
        contentPadding = PaddingValues(UiSpacing.page),
        verticalArrangement = Arrangement.spacedBy(UiSpacing.section)
    ) {
        item {
            TextButton(onClick = onBack) {
                Text("← ${strings.back}")
            }
        }
        item { ScreenName(title = strings.legalAndDataSources) }
        item {
            Text(
                text = strings.legalInfoText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LegalNoticeKind.entries.forEach { kind ->
            val rows = notices.filter { it.kind == kind }
            if (rows.isNotEmpty()) {
                item {
                    ShelfHeader(
                        title = when (kind) {
                            LegalNoticeKind.DataSource -> strings.dataSources
                            LegalNoticeKind.SoftwareLicense -> strings.softwareLicenses
                            LegalNoticeKind.ServiceTerms -> strings.serviceTerms
                        }
                    )
                }
                items(rows, key = { it.id }) { notice ->
                    LegalNoticeCard(
                        notice = notice,
                        openText = strings.openLink,
                        onOpen = { runCatching { uriHandler.openUri(notice.url) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun LegalNoticeCard(
    notice: LegalNotice,
    openText: String,
    onOpen: () -> Unit
) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = notice.terms,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = notice.details,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onOpen, shape = MaterialTheme.shapes.small) {
                Text(openText)
            }
        }
    }
}
