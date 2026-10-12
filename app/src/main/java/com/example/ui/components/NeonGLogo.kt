package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBorderGlowing
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.ObsidianBlack

@Composable
fun NeonGLogo(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    showGlow: Boolean = true,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon_pulse")
    val pulseAlpha by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
    } else {
        rememberInfiniteTransition(label = "static").animateFloat(
            initialValue = 0.8f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "staticAlpha"
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("neon_g_logo"),
        contentAlignment = Alignment.Center
    ) {
        // Outer Glowing Backdrop
        if (showGlow) {
            Box(
                modifier = Modifier
                    .size(size * 1.15f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.45f * pulseAlpha),
                                NeonElectricBlue.copy(alpha = 0.2f * pulseAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Geometric Cyber Shield Container
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.28f))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0D182B),
                            ObsidianBlack
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = pulseAlpha),
                            NeonElectricBlue.copy(alpha = 0.4f),
                            NeonIndigo.copy(alpha = 0.8f * pulseAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(size * 0.28f)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Neon Cyber 'G' Glyphic Drawing
            Canvas(
                modifier = Modifier
                    .size(size * 0.68f)
            ) {
                val w = this.size.width
                val h = this.size.height
                val strokeW = w * 0.18f

                // Outer Neon Glow Pass
                val glowBrush = Brush.linearGradient(
                    colors = listOf(NeonCyan, NeonElectricBlue, NeonIndigo)
                )

                // Draw Custom Futuristic Cyber "G" Path
                val path = Path().apply {
                    // Top curve down around to bottom and into crossbar
                    moveTo(w * 0.85f, h * 0.28f)
                    cubicTo(w * 0.75f, h * 0.06f, w * 0.25f, h * 0.06f, w * 0.15f, h * 0.35f)
                    cubicTo(w * 0.05f, h * 0.65f, w * 0.25f, h * 0.94f, w * 0.65f, h * 0.94f)
                    cubicTo(w * 0.82f, h * 0.94f, w * 0.90f, h * 0.82f, w * 0.90f, h * 0.58f)
                    lineTo(w * 0.52f, h * 0.58f)
                }

                drawPath(
                    path = path,
                    brush = glowBrush,
                    style = Stroke(
                        width = strokeW,
                        cap = StrokeCap.Round
                    )
                )

                // Inner Bright Core for Neon Tube effect
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(
                        width = strokeW * 0.35f,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}
