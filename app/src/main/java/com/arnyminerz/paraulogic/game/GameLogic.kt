package com.arnyminerz.paraulogic.game

import androidx.annotation.IntRange
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord

fun List<IntroducedWord>.getTutis(gameInfo: GameInfo): List<IntroducedWord> {
    val tutis = arrayListOf<IntroducedWord>()
    for (word in this)
        if (gameInfo.isTuti(word.word))
            tutis.add(word)
    return tutis
}

/**
 * Calculates the amount of points are obtained from the current instance String.
 * @author Arnau Mora
 * @since 20220309
 * @param gameInfo The game information for checking for tutis.
 * @return The amount of points the current instance word gives.
 */
fun String.getPoints(gameInfo: GameInfo): Int =
    when (val len = length) {
        3 -> 1
        4 -> 2
        else -> len + (if (gameInfo.isTuti(this)) 10 else 0)
    }

fun List<IntroducedWord>.calculatePoints(gameInfo: GameInfo): Int {
    var points = 0
    for (iWord in this) {
        val word = iWord.word
        points += word.getPoints(gameInfo)
    }
    return points
}

@IntRange(from = 0L, to = (AMOUNT_OF_LEVELS - 1).toLong())
fun getLevelFromPoints(points: Int, pointsPerLevel: Int): Int =
    when {
        points < pointsPerLevel -> 0
        points < 2 * pointsPerLevel -> 1
        points < 3 * pointsPerLevel -> 2
        points < 4 * pointsPerLevel -> 3
        points < 5 * pointsPerLevel -> 4
        points < 6 * pointsPerLevel -> 5
        else -> 6
    }
