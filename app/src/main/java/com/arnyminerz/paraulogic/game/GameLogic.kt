package com.arnyminerz.paraulogic.game

import com.arnyminerz.paraulogic.storage.entity.IntroducedWord

fun List<IntroducedWord>.getTutis(gameInfo: GameInfo): List<IntroducedWord> {
    val tutis = arrayListOf<IntroducedWord>()
    for (word in this)
        if (gameInfo.isTuti(word.word))
            tutis.add(word)
    return tutis
}

fun List<IntroducedWord>.calculatePoints(gameInfo: GameInfo): Int {
    var points = 0
    for (iWord in this) {
        val len = iWord.word.length
        points += when {
            len == 3 -> 1
            len == 4 -> 2
            gameInfo.isTuti(iWord.word) -> 10
            else -> iWord.word.length
        }
    }
    return points
}
