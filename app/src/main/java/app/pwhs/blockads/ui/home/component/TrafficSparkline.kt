package app.pwhs.blockads.ui.home.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun TrafficSparkline(
    points: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return@Canvas

        val hasActiveData = points.isNotEmpty() && points.any { it > 0.15f }
        val path = Path()

        if (!hasActiveData) {
            // Precise signature curve matching user mockup: flat baseline -> smooth S-curve rise -> flat plateau
            val yLow = height * 0.75f
            val yHigh = height * 0.25f
            val xStartCurve = width * 0.65f
            val xEndCurve = width * 0.85f

            path.moveTo(0f, yLow)
            path.lineTo(xStartCurve, yLow)
            path.cubicTo(
                xStartCurve + (xEndCurve - xStartCurve) * 0.5f, yLow,
                xStartCurve + (xEndCurve - xStartCurve) * 0.5f, yHigh,
                xEndCurve, yHigh
            )
            path.lineTo(width, yHigh)
        } else {
            val stepX = width / (points.size - 1).coerceAtLeast(1)
            val coords = points.mapIndexed { index, fraction ->
                val x = index * stepX
                val y = height - (fraction.coerceIn(0.15f, 0.85f) * height)
                Offset(x, y)
            }

            path.moveTo(coords.first().x, coords.first().y)
            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val midX = p0.x + (p1.x - p0.x) / 2f
                path.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            }
        }

        // Soft outer neon glow
        drawPath(
            path = path,
            color = color.copy(alpha = 0.18f),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Mid glow
        drawPath(
            path = path,
            color = color.copy(alpha = 0.40f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Core sharp line
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
