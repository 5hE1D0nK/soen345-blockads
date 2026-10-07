package app.pwhs.blockads.ui.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
 * Inspired by Crystal Scan Paywall: uses two opposing diagonal radial gradient orbs
 * (top-right and bottom-left) that breathe with a waveOffset animation and react to VPN status.
 */
@Composable
fun HomeAmbientBackground(
    vpnEnabled: Boolean,
    vpnConnecting: Boolean,
    vpnStopping: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val primaryAccent = MaterialTheme.colorScheme.primary

    // Dynamic color pairs based on VPN protection state
    val (targetPrimary, targetSecondary) = when {
        vpnStopping -> Pair(AccentOrange, DangerRed)
        vpnConnecting -> Pair(AccentBlue, Color(0xFF38BDF8))
        vpnEnabled -> Pair(primaryAccent, AccentTeal)
        else -> Pair(DangerRed, AccentOrange)
    }

    val colorTransitionSpec = tween<Color>(durationMillis = 800)
    val tint by animateColorAsState(targetValue = targetPrimary, animationSpec = colorTransitionSpec, label = "tint")
    val secondaryTint by animateColorAsState(targetValue = targetSecondary, animationSpec = colorTransitionSpec, label = "secondaryTint")

    // Wave animation matching Crystal Scan Paywall
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )

    // Opacity: 0.15f / 0.10f in light mode, slightly deeper in dark mode
    val primaryAlpha = if (isDark) 0.20f else 0.15f
    val secondaryAlpha = if (isDark) 0.15f else 0.10f

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // Top-right primary orb
        val center1 = Offset(
            x = w * 0.8f + (waveOffset * 0.2f),
            y = h * 0.1f + (waveOffset * 0.3f)
        )
        val radius1 = (w * 0.75f).coerceAtLeast(800f) + (waveOffset * 0.5f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tint.copy(alpha = primaryAlpha), Color.Transparent),
                center = center1,
                radius = radius1
            ),
            radius = radius1,
            center = center1
        )

        // Bottom-left secondary orb
        val center2 = Offset(
            x = w * 0.2f - (waveOffset * 0.4f),
            y = h * 0.8f - (waveOffset * 0.2f)
        )
        val radius2 = (w * 0.60f).coerceAtLeast(600f) - (waveOffset * 0.3f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondaryTint.copy(alpha = secondaryAlpha), Color.Transparent),
                center = center2,
                radius = radius2
            ),
            radius = radius2,
            center = center2
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
