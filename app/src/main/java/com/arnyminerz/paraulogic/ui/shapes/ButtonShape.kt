package com.arnyminerz.paraulogic.ui.shapes

import androidx.compose.foundation.shape.GenericShape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws an hexagonal shape.
 * @author Arnau Mora
 * @since 20220307
 * @see <a href="https://stackoverflow.com/a/11354824/5717211">Reference code</a>
 */
val HexagonalShape = GenericShape { boxSize, _ ->
    val size = boxSize.width / 2
    val center = boxSize.width / 2

    fun getHexCornerCord(center: Float, i: Int): Pair<Float, Float> {
        val angleDeg = 60 * i - 30
        val angleRad = PI / 180 * angleDeg
        val x = center + size * cos(angleRad)
        val y = center + size * sin(angleRad)
        return x.toFloat() to y.toFloat()
    }

    val start = getHexCornerCord(center, 0)
    moveTo(start.first, start.second)
    for (i in 0 until 6) {
        val point = getHexCornerCord(center, i + 1)
        lineTo(point.first, point.second)
    }

    close()
}
