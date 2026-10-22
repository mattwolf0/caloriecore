package app.caloriecore.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object LineIcons {
    val Food: ImageVector by lazy {
        makeIcon("Food") {
            moveTo(6f, 3f)
            verticalLineTo(8f)
            moveTo(10f, 3f)
            verticalLineTo(8f)
            moveTo(6f, 8f)
            quadTo(6f, 11f, 8f, 11f)
            quadTo(10f, 11f, 10f, 8f)
            moveTo(8f, 3f)
            verticalLineTo(21f)
            moveTo(17f, 21f)
            verticalLineTo(3f)
            quadTo(20f, 5f, 20f, 11f)
            lineTo(17f, 12f)
        }
    }

    val Train: ImageVector by lazy {
        makeIcon("Train") {
            moveTo(8f, 12f)
            horizontalLineTo(16f)
            moveTo(7f, 7f)
            verticalLineTo(17f)
            moveTo(17f, 7f)
            verticalLineTo(17f)
            moveTo(4f, 9.5f)
            verticalLineTo(14.5f)
            moveTo(20f, 9.5f)
            verticalLineTo(14.5f)
        }
    }

    val Progress: ImageVector by lazy {
        makeIcon("Progress") {
            moveTo(3f, 17f)
            lineTo(9f, 11f)
            lineTo(13f, 15f)
            lineTo(21f, 7f)
            moveTo(15f, 7f)
            horizontalLineTo(21f)
            verticalLineTo(13f)
        }
    }

    val Today: ImageVector by lazy {
        makeIcon("Today") {
            moveTo(12f, 3f)
            arcTo(9f, 9f, 0f, true, true, 11.99f, 3f)
            moveTo(12f, 7.5f)
            verticalLineTo(12f)
            lineTo(15f, 14f)
        }
    }

    val Steps: ImageVector by lazy {
        makeIcon("Steps") {
            moveTo(8f, 3.5f)
            curveTo(10.5f, 3.5f, 11f, 7f, 10.5f, 10f)
            lineTo(10f, 13f)
            horizontalLineTo(6f)
            curveTo(5f, 9f, 5f, 3.5f, 8f, 3.5f)
            moveTo(6.2f, 16f)
            horizontalLineTo(9.8f)
            verticalLineTo(17.5f)
            quadTo(8f, 19.5f, 6.2f, 17.5f)
            close()
            moveTo(16f, 7.5f)
            curveTo(13.5f, 7.5f, 13f, 11f, 13.5f, 14f)
            lineTo(14f, 17f)
            horizontalLineTo(18f)
            curveTo(19f, 13f, 19f, 7.5f, 16f, 7.5f)
            moveTo(14.2f, 20f)
            horizontalLineTo(17.8f)
        }
    }

    val Scan: ImageVector by lazy {
        makeIcon("Scan") {
            moveTo(3f, 8f)
            verticalLineTo(5f)
            quadTo(3f, 3f, 5f, 3f)
            horizontalLineTo(8f)
            moveTo(16f, 3f)
            horizontalLineTo(19f)
            quadTo(21f, 3f, 21f, 5f)
            verticalLineTo(8f)
            moveTo(21f, 16f)
            verticalLineTo(19f)
            quadTo(21f, 21f, 19f, 21f)
            horizontalLineTo(16f)
            moveTo(8f, 21f)
            horizontalLineTo(5f)
            quadTo(3f, 21f, 3f, 19f)
            verticalLineTo(16f)
            moveTo(7f, 8f)
            verticalLineTo(16f)
            moveTo(10.5f, 8f)
            verticalLineTo(16f)
            moveTo(13.5f, 8f)
            verticalLineTo(16f)
            moveTo(17f, 8f)
            verticalLineTo(16f)
        }
    }

    val Copy: ImageVector by lazy {
        makeIcon("Copy") {
            moveTo(9f, 9f)
            horizontalLineTo(19f)
            verticalLineTo(19f)
            horizontalLineTo(9f)
            close()
            moveTo(5f, 15f)
            verticalLineTo(5f)
            horizontalLineTo(15f)
        }
    }

    val Language: ImageVector by lazy {
        makeIcon("Language") {
            moveTo(12f, 3f)
            arcTo(9f, 9f, 0f, true, true, 11.99f, 3f)
            moveTo(3f, 12f)
            horizontalLineTo(21f)
            moveTo(12f, 3f)
            quadTo(16f, 7.5f, 16f, 12f)
            quadTo(16f, 16.5f, 12f, 21f)
            quadTo(8f, 16.5f, 8f, 12f)
            quadTo(8f, 7.5f, 12f, 3f)
        }
    }

    val Theme: ImageVector by lazy {
        makeIcon("Theme") {
            moveTo(20f, 14.5f)
            arcTo(8.5f, 8.5f, 0f, true, true, 9.5f, 4f)
            arcTo(7f, 7f, 0f, false, false, 20f, 14.5f)
            close()
        }
    }

    private fun makeIcon(name: String, pathBuilder: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = pathBuilder
        ).build()
}
