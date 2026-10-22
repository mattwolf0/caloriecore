package app.caloriecore.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.PagePadding
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.text.CalorieCoreStrings

@Composable
internal fun LegalInfoScreen(strings: CalorieCoreStrings, onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val notices = legalNotices(strings)

    BackHandler(onBack = onBack)

    LazyColumn(
        contentPadding = PagePadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back)
                }
                Text(
                    text = strings.legalAndDataSources,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
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
private fun LegalNoticeCard(notice: LegalNotice, openText: String, onOpen: () -> Unit) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium
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
            TextButton(onClick = onOpen) {
                Text(openText)
            }
        }
    }
}
