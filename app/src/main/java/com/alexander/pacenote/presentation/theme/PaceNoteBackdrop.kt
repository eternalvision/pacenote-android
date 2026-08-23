package com.alexander.pacenote.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

@Composable
fun PaceNoteBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Midnight)
                .drawBehind { drawWorkspaceGrid() },
            content = content,
        )
    }
}

private fun DrawScope.drawWorkspaceGrid() {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Frost.copy(alpha = 0.045f), Color.Transparent),
            center = Offset(size.width * 0.14f, size.height * 0.04f),
            radius = size.minDimension * 0.7f,
        ),
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Frost.copy(alpha = 0.025f), Color.Transparent),
            center = Offset(size.width * 0.92f, size.height * 0.88f),
            radius = size.minDimension * 0.76f,
        ),
    )

    val step = 48.dp.toPx()
    val gridColor = Frost.copy(alpha = 0.035f)
    var x = 0f
    while (x <= size.width) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        x += step
    }
    var y = 0f
    while (y <= size.height) {
        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        y += step
    }
}
