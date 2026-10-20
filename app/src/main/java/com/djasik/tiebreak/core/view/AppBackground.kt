package com.djasik.tiebreak.core.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val purple = Color(0xFF24104F)
            val lightPurple = Color(0xFF3A176F)

            drawRect(
                color = purple,
            )

            val topShape = Path().apply {
                moveTo(size.width * 0.55f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.28f)
                close()
            }

            drawPath(
                path = topShape,
                color = lightPurple,
            )

            val bottomShape = Path().apply {
                moveTo(0f, size.height * 0.78f)
                lineTo(size.width, size.height * 0.62f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            drawPath(
                path = bottomShape,
                color = Color(0xFF2D125D),
            )
        }

        content()
    }
}