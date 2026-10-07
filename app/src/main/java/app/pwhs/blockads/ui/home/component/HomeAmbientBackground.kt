package app.pwhs.blockads.ui.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import app.pwhs.blockads.ui.theme.AccentBlue
import app.pwhs.blockads.ui.theme.AccentOrange
import app.pwhs.blockads.ui.theme.AccentTeal
import app.pwhs.blockads.ui.theme.BlockadsTheme
import app.pwhs.blockads.ui.theme.DangerRed
import app.pwhs.blockads.ui.theme.NeonGreen

/**
 * Ambient floating glow background for the Home Screen.
 * Renders subtle animated radial gradient orbs that shift position, scale, and color
 * dynamically based on the VPN protection state.
 */
@Composable
fun HomeAmbientBackground(
    vpnEnabled: Boolean,
    vpnConnecting: Boolean,
    vpnStopping: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // Determine target colors based on VPN status
    val (targetColor1, targetColor2, targetColor3) = when {
        vpnStopping -> Triple(
            AccentOrange,
            DangerRed,
            Color(0xFFEAB308) // Amber
        )
        vpnConnecting -> Triple(
            AccentBlue,
            Color(0xFF38BDF8), // Sky Blue
            Color(0xFF818CF8)  // Indigo
        )
        vpnEnabled -> Triple(
            NeonGreen,
            AccentTeal,
            Color(0xFF10B981) // Emerald
        )
        else -> Triple(
            DangerRed,
            Color(0xFFA855F7), // Purple
            AccentOrange
        )
    }

    val colorTransitionSpec = tween<Color>(durationMillis = 1000)
    val color1 by animateColorAsState(targetValue = targetColor1, animationSpec = colorTransitionSpec, label = "orbColor1")
    val color2 by animateColorAsState(targetValue = targetColor2, animationSpec = colorTransitionSpec, label = "orbColor2")
    val color3 by animateColorAsState(targetValue = targetColor3, animationSpec = colorTransitionSpec, label = "orbColor3")

    val infiniteTransition = rememberInfiniteTransition(label = "ambientOrbs")

    // Breathing pulse animations
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1.12f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )

    // Floating orbital drift offsets
    val driftX1 by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftX1"
    )

    val driftY1 by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY1"
    )

    val driftX2 by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftX2"
    )

    val driftY2 by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY2"
    )

    // Base opacity: vibrant in dark theme, soft pastel in light theme
    val baseAlpha = if (isDark) 0.18f else 0.08f

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // Orb 1: Upper-center behind Power Button & Status Header
        val center1 = Offset(w * 0.5f + driftX1, h * 0.22f + driftY1)
        val radius1 = (w * 0.65f * pulse1).coerceAtLeast(10f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1.copy(alpha = baseAlpha * 1.15f), Color.Transparent),
                center = center1,
                radius = radius1
            ),
            radius = radius1,
            center = center1
        )

        // Orb 2: Upper-right floating accent
        val center2 = Offset(w * 0.85f + driftX2, h * 0.42f + driftY2)
        val radius2 = (w * 0.55f * pulse2).coerceAtLeast(10f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2.copy(alpha = baseAlpha * 0.95f), Color.Transparent),
                center = center2,
                radius = radius2
            ),
            radius = radius2,
            center = center2
        )

        // Orb 3: Lower-left grounding ambient
        val center3 = Offset(w * 0.15f - driftX1 * 0.5f, h * 0.72f - driftY1 * 0.5f)
        val radius3 = (w * 0.62f * pulse1).coerceAtLeast(10f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color3.copy(alpha = baseAlpha * 0.85f), Color.Transparent),
                center = center3,
                radius = radius3
            ),
            radius = radius3,
            center = center3
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeAmbientBackgroundProtectedPreview() {
    BlockadsTheme {
        HomeAmbientBackground(
            vpnEnabled = true,
            vpnConnecting = false,
            vpnStopping = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeAmbientBackgroundUnprotectedPreview() {
    BlockadsTheme {
        HomeAmbientBackground(
            vpnEnabled = false,
            vpnConnecting = false,
            vpnStopping = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}
