package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * GitHub-style commit heatmap grid.
 * Each cell = one week, color intensity = commit count relative to max.
 * Renders as a single-row grid since the participation API gives weekly totals.
 */
@Composable
fun CommitHeatmap(
    weeklyData: List<Int>,
    modifier: Modifier = Modifier,
) {
    if (weeklyData.isEmpty()) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelSizeSp = 10.sp

    Canvas(modifier = modifier) {
        val maxVal = weeklyData.max().coerceAtLeast(1)
        val labelFontSize = labelSizeSp.toPx()
        val bottomMargin = labelFontSize + 8.dp.toPx()
        val topMargin = 0f

        val cellSpacing = 2.dp.toPx()
        val availableWidth = size.width
        val availableHeight = size.height - bottomMargin - topMargin

        // Calculate cell size to fit all weeks
        val numWeeks = weeklyData.size
        val cellSize = ((availableWidth - (numWeeks - 1) * cellSpacing) / numWeeks)
            .coerceAtMost(availableHeight) // Don't make cells taller than the height
            .coerceAtLeast(4.dp.toPx())    // Minimum visible size
        val cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())

        // Center vertically if cells are shorter than available height
        val yOffset = topMargin + (availableHeight - cellSize) / 2f

        // Draw cells
        weeklyData.forEachIndexed { i, commits ->
            val x = i * (cellSize + cellSpacing)
            val intensity = commits.toFloat() / maxVal

            val cellColor = if (commits == 0) {
                emptyColor
            } else {
                // Lerp from low (0.15 alpha) to full intensity
                primaryColor.copy(alpha = 0.15f + 0.85f * intensity)
            }

            drawRoundRect(
                color = cellColor,
                topLeft = Offset(x, yOffset),
                size = Size(cellSize, cellSize),
                cornerRadius = cornerRadius,
            )
        }

        // X-axis month labels
        val xLabelPaint = android.graphics.Paint().apply {
            color = labelColor.hashCode()
            textSize = labelFontSize
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        val today = LocalDate.now()
        val weeksTotal = weeklyData.size
        // Show month labels — at the start of each new month
        var lastMonth = -1
        for (i in weeklyData.indices) {
            val weeksAgo = (weeksTotal - 1 - i).toLong()
            val date = today.minusWeeks(weeksAgo)
            val month = date.monthValue

            if (month != lastMonth) {
                lastMonth = month
                val monthLabel = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val x = i * (cellSize + cellSpacing) + cellSize / 2
                // Only draw if there's enough space (avoid overlapping at the edges)
                if (x > labelFontSize * 2 && x < availableWidth - labelFontSize * 2) {
                    drawContext.canvas.nativeCanvas.drawText(
                        monthLabel,
                        x,
                        yOffset + cellSize + bottomMargin - 2.dp.toPx(),
                        xLabelPaint,
                    )
                }
            }
        }
    }
}

/**
 * Intensity legend for the heatmap: "Less" [□ ▪ ▪ ▪ ■] "More"
 */
@Composable
fun HeatmapLegend(
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelSizeSp = 10.sp

    Canvas(modifier = modifier) {
        val labelFontSize = labelSizeSp.toPx()
        val cellSize = 10.dp.toPx()
        val cellSpacing = 2.dp.toPx()
        val cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())

        val textPaint = android.graphics.Paint().apply {
            color = labelColor.hashCode()
            textSize = labelFontSize
            isAntiAlias = true
        }

        val lessWidth = textPaint.measureText("Less")
        val moreWidth = textPaint.measureText("More")

        // Total width: "Less" + 5 cells + "More"
        val totalWidth = lessWidth + 8.dp.toPx() + 5 * cellSize + 4 * cellSpacing + 8.dp.toPx() + moreWidth
        val startX = (size.width - totalWidth) / 2
        val yCenter = size.height / 2

        // "Less"
        drawContext.canvas.nativeCanvas.drawText(
            "Less",
            startX,
            yCenter + labelFontSize * 0.35f,
            textPaint,
        )

        // 5 cells at increasing intensity
        val intensities = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
        var cellX = startX + lessWidth + 8.dp.toPx()
        for (intensity in intensities) {
            val color = if (intensity == 0f) emptyColor
            else primaryColor.copy(alpha = 0.15f + 0.85f * intensity)
            drawRoundRect(
                color = color,
                topLeft = Offset(cellX, yCenter - cellSize / 2),
                size = Size(cellSize, cellSize),
                cornerRadius = cornerRadius,
            )
            cellX += cellSize + cellSpacing
        }

        // "More"
        drawContext.canvas.nativeCanvas.drawText(
            "More",
            cellX + 4.dp.toPx(),
            yCenter + labelFontSize * 0.35f,
            textPaint,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CommitHeatmapPreview() {
    MyApplicationTheme {
        CommitHeatmap(
            weeklyData = listOf(
                0, 0, 3, 7, 2, 10, 5, 8, 4, 6, 12, 3, 5, 9,
                7, 2, 11, 6, 4, 8, 3, 15, 5, 7, 2, 10,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun HeatmapLegendPreview() {
    MyApplicationTheme {
        HeatmapLegend(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
        )
    }
}
