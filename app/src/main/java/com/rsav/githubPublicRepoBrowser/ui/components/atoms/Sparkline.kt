package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun Sparkline(
    data: List<Int>,
    modifier: Modifier = Modifier,
) {
    if (data.size < 2) return

    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val fillColor = lineColor.copy(alpha = 0.08f)
    val dotColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelSizeSp = 10.sp

    Canvas(modifier = modifier) {
        val maxVal = data.max().coerceAtLeast(1)
        val labelFontSize = labelSizeSp.toPx()
        val dotRadius = 3.dp.toPx()
        val labelPaddingAboveDot = 2.dp.toPx()

        // Margins for axis labels
        val leftMargin = 32.dp.toPx()
        val bottomMargin = 18.dp.toPx()
        val topMargin = labelFontSize + labelPaddingAboveDot + dotRadius + 4.dp.toPx()

        val chartWidth = size.width - leftMargin
        val chartHeight = size.height - topMargin - bottomMargin
        val stepX = chartWidth / (data.size - 1)

        // Paint for text labels
        android.graphics.Paint().apply {
            color = labelColor.hashCode()
            textSize = labelFontSize
            isAntiAlias = true
            typeface = Typeface.DEFAULT
        }
        val labelTextPaint = android.graphics.Paint().apply {
            color = dotColor.hashCode()
            textSize = labelFontSize
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // Compute points
        val points = data.mapIndexed { i, value ->
            Offset(
                x = leftMargin + i * stepX,
                y = topMargin + chartHeight * (1f - value / maxVal.toFloat()),
            )
        }

        // --- Y axis ---
        // Axis line
        drawLine(axisColor, Offset(leftMargin, topMargin), Offset(leftMargin, topMargin + chartHeight), 1.dp.toPx())

        // Y tick labels: 0 and max
        val yAxisPaint = android.graphics.Paint().apply {
            color = labelColor.hashCode()
            textSize = labelFontSize
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        drawContext.canvas.nativeCanvas.drawText(
            maxVal.toString(),
            leftMargin - 4.dp.toPx(),
            topMargin + labelFontSize * 0.35f,
            yAxisPaint,
        )
        drawContext.canvas.nativeCanvas.drawText(
            "0",
            leftMargin - 4.dp.toPx(),
            topMargin + chartHeight + labelFontSize * 0.35f,
            yAxisPaint,
        )
        // Mid tick
        val midVal = maxVal / 2
        if (midVal > 0) {
            val midY = topMargin + chartHeight * 0.5f
            drawLine(axisColor, Offset(leftMargin, midY), Offset(leftMargin + chartWidth, midY), 0.5.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(
                midVal.toString(),
                leftMargin - 4.dp.toPx(),
                midY + labelFontSize * 0.35f,
                yAxisPaint,
            )
        }

        // --- X axis ---
        val xAxisY = topMargin + chartHeight
        drawLine(axisColor, Offset(leftMargin, xAxisY), Offset(size.width, xAxisY), 1.dp.toPx())

        // X labels: month names based on week offsets from today
        val today = LocalDate.now()
        val weeksTotal = data.size
        // Show labels roughly every 4 weeks (monthly)
        val xLabelPaint = android.graphics.Paint().apply {
            color = labelColor.hashCode()
            textSize = labelFontSize
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        for (i in data.indices step 4) {
            val weeksAgo = (weeksTotal - 1 - i).toLong()
            val date = today.minusWeeks(weeksAgo)
            val monthLabel = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val x = points[i].x
            drawContext.canvas.nativeCanvas.drawText(
                monthLabel,
                x,
                xAxisY + bottomMargin - 2.dp.toPx(),
                xLabelPaint,
            )
            // Small tick mark
            drawLine(axisColor, Offset(x, xAxisY), Offset(x, xAxisY + 3.dp.toPx()), 0.5.dp.toPx())
        }

        // --- Fill area ---
        val fillPath = Path().apply {
            moveTo(points.first().x, xAxisY)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, xAxisY)
            close()
        }
        drawPath(fillPath, Brush.verticalGradient(listOf(fillColor, fillColor.copy(alpha = 0f))))

        // --- Line ---
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(
            linePath,
            lineColor,
            style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // --- Dots + labels at upticks (where value increased from previous) ---
        // Track last drawn label X to avoid overlap
        var lastLabelX = -Float.MAX_VALUE
        val minLabelSpacing = 24.dp.toPx()

        for (i in 1 until data.size) {
            if (data[i] > data[i - 1] && data[i] > 0) {
                val pt = points[i]

                // Dot
                drawCircle(dotColor, dotRadius, pt)

                // Label (skip if too close to previous label)
                if (pt.x - lastLabelX >= minLabelSpacing) {
                    drawContext.canvas.nativeCanvas.drawText(
                        data[i].toString(),
                        pt.x,
                        pt.y - dotRadius - labelPaddingAboveDot,
                        labelTextPaint,
                    )
                    lastLabelX = pt.x
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SparklinePreview() {
    MyApplicationTheme {
        Sparkline(
            data = listOf(
                3, 7, 2, 10, 5, 8, 4, 6, 12, 3, 5, 9,
                7, 2, 11, 6, 4, 8, 3, 15, 5, 7, 2, 10,
                8, 3, 6, 14, 5, 9, 3, 7, 11, 4, 6, 8,
                2, 10, 5, 3, 7, 12, 6, 4, 9, 3, 8, 5,
                7, 2, 11, 6,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SparklineSmallPreview() {
    MyApplicationTheme {
        Sparkline(
            data = listOf(3, 7, 2, 10, 5, 8, 4, 6),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        )
    }
}
