package app.pwhs.blockads.ui.statistics.destinations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.pwhs.blockads.data.entities.CountryStat
import app.pwhs.blockads.data.geoip.GeoIpLookup
import app.pwhs.blockads.ui.theme.AccentBlue
import kotlin.math.hypot

/**
 * Interactive World Map Canvas combining NextDNS choropleth colors with NordVPN-style radar dots.
 */
@Composable
fun WorldMapCanvas(
    countryStats: List<CountryStat>,
    modifier: Modifier = Modifier,
    selectedCountryIso: String? = null,
    onCountrySelected: (String?) -> Unit = {}
) {
    val context = LocalContext.current
    var shapes by remember { mutableStateOf<List<CountryShape>>(emptyList()) }

    LaunchedEffect(Unit) {
        shapes = WorldMapData.load(context)
    }

    val statsMap = remember(countryStats) {
        countryStats.associate { it.countryCode.uppercase() to it.count }
    }
    val totalQueries = remember(countryStats) {
        countryStats.sumOf { it.count }.coerceAtLeast(1)
    }
    val maxCount = remember(countryStats) {
        countryStats.maxOfOrNull { it.count } ?: 1
    }

    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing radar animation for active destinations (NordVPN style)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_progress"
    )

    val oceanColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val idleLandColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val landBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val activeAccentColor = AccentBlue

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f)
            .clip(RoundedCornerShape(16.dp))
            .background(oceanColor)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)
                    val maxPanX = (size.width * (scale - 1f)) / 2f
                    val maxPanY = (size.height * (scale - 1f)) / 2f
                    offsetX = (offsetX + pan.x).coerceIn(-maxPanX, maxPanX)
                    offsetY = (offsetY + pan.y).coerceIn(-maxPanY, maxPanY)
                }
            }
            .pointerInput(shapes, scale, offsetX, offsetY) {
                detectTapGestures { tapOffset ->
                    val mapWidth = size.width
                    val mapHeight = size.height

                    // Convert screen tap coordinates to original 1000x500 map coordinates
                    val centerX = mapWidth / 2f
                    val centerY = mapHeight / 2f
                    val unscaledX = (tapOffset.x - centerX - offsetX) / scale + centerX
                    val unscaledY = (tapOffset.y - centerY - offsetY) / scale + centerY

                    val mapX = (unscaledX / mapWidth) * 1000f
                    val mapY = (unscaledY / mapHeight) * 500f

                    // Find closest country centroid within radius
                    val hit = shapes.minByOrNull { shape ->
                        hypot(shape.centroid.x - mapX, shape.centroid.y - mapY)
                    }

                    if (hit != null && hypot(hit.centroid.x - mapX, hit.centroid.y - mapY) < 65f) {
                        onCountrySelected(hit.iso)
                    } else {
                        onCountrySelected(null)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val scaleX = canvasW / 1000f
            val scaleY = canvasH / 500f

            translate(left = offsetX, top = offsetY) {
                scale(scale = scale, pivot = center) {
                    scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
                        for (country in shapes) {
                            val count = statsMap[country.iso]
                            val isSelected = country.iso == selectedCountryIso

                            val fillColor = when {
                                isSelected -> Color(0xFF00E676)
                                count != null -> {
                                    val ratio = (count.toFloat() / maxCount).coerceIn(0.25f, 1f)
                                    activeAccentColor.copy(alpha = 0.35f + ratio * 0.65f)
                                }
                                else -> idleLandColor
                            }

                            drawPath(
                                path = country.path,
                                color = fillColor,
                                style = Fill
                            )
                            drawPath(
                                path = country.path,
                                color = if (isSelected) Color(0xFF00E676) else landBorderColor,
                                style = Stroke(width = if (isSelected) 1.5f else 0.5f)
                            )
                        }

                        // Draw glowing radar pulse dots on countries with traffic
                        drawRadarPins(
                            shapes = shapes,
                            statsMap = statsMap,
                            pulseProgress = pulseProgress,
                            accentColor = activeAccentColor
                        )
                    }
                }
            }
        }

        // Reset zoom button when zoomed in
        if (scale > 1.05f) {
            IconButton(
                onClick = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset Zoom",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Selected Country Tooltip
        selectedCountryIso?.let { iso ->
            val count = statsMap[iso] ?: 0
            val percent = if (totalQueries > 0) (count * 100f / totalQueries) else 0f
            val emoji = GeoIpLookup.countryCodeToEmoji(iso)
            val name = GeoIpLookup.getCountryName(iso)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "$emoji $name  •  $count (${"%.1f".format(percent)}%)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

private fun DrawScope.drawRadarPins(
    shapes: List<CountryShape>,
    statsMap: Map<String, Int>,
    pulseProgress: Float,
    accentColor: Color
) {
    for (shape in shapes) {
        if (statsMap.containsKey(shape.iso)) {
            val center = shape.centroid

            // Pulsing outer radar ring
            val pulseRadius = 5f + pulseProgress * 18f
            val pulseAlpha = (1f - pulseProgress).coerceIn(0f, 1f) * 0.7f
            drawCircle(
                color = accentColor.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Inner glowing solid dot
            drawCircle(
                color = accentColor,
                radius = 3.5f,
                center = center
            )
        }
    }
}
