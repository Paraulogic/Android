package com.arnyminerz.paraulogic.game

import com.arnyminerz.paraulogic.storage.entity.IntroducedWord

fun List<IntroducedWord>.getTutis(gameInfo: GameInfo): List<IntroducedWord> {
    val tutis = arrayListOf<IntroducedWord>()
    for (word in this)
        if (gameInfo.isTuti(word.word))
            tutis.add(word)
    return tutis
}
