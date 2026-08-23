package com.alexander.pacenote.presentation.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.random.Random

fun Modifier.noiseTexture(
    seed: Int = 23,
    alpha: Float = 0.045f,
): Modifier = drawWithCache {
    val random = Random(seed + size.width.roundToInt() * 31 + size.height.roundToInt())
    val pointCount = (size.width * size.height / 210f).roundToInt().coerceIn(90, 1_450)
    val lightPoints = List(pointCount) {
        Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
    }
    val darkPoints = List(pointCount / 2) {
        Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
    }

    onDrawBehind {
        drawPoints(
            points = lightPoints,
            pointMode = PointMode.Points,
            color = Frost.copy(alpha = alpha),
            strokeWidth = 1f,
        )
        drawPoints(
            points = darkPoints,
            pointMode = PointMode.Points,
            color = Color.Black.copy(alpha = alpha * 0.8f),
            strokeWidth = 1f,
        )
    }
}

@Composable
fun NoiseSurface(
    modifier: Modifier = Modifier,
    color: Color = Graphite,
    shape: Shape = MaterialTheme.shapes.large,
    border: BorderStroke? = BorderStroke(1.dp, Hairline),
    seed: Int = 23,
    noiseAlpha: Float = 0.045f,
    shadowElevation: Dp = 2.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = shape,
        border = border,
        tonalElevation = 0.dp,
        shadowElevation = shadowElevation,
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .noiseTexture(seed = seed, alpha = noiseAlpha),
            content = content,
        )
    }
}
