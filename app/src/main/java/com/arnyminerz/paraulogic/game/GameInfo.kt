package com.arnyminerz.paraulogic.game

import androidx.compose.runtime.MutableState
import com.arnyminerz.paraulogic.crypto.md5

data class GameInfo(
    var letters: MutableState<List<Char>>,
    val centerLetter: Char,
    val words: Map<String, String>,
) {
    override fun toString(): String =
        "Letters: $letters. Center: $centerLetter. Words: $words"

    /**
     * Mixes up the [letters] list.
     * @author Arnau Mora
     * @since 20220307
     */
    fun shuffle() {
        letters.value = letters.value.shuffled()
    }

    /**
     * Returns a unique hash of the game info.
     * @author Arnau Mora
     * @since 20220307
     */
    fun hash(): String {
        val sortedLetters = letters.value.sorted()
        val sortedWords = words.entries.sortedBy { it.key }.map { it.key + ":" + it.value }
        val toHash = sortedLetters.joinToString(";") +
                sortedWords.joinToString(";") +
                centerLetter
        return md5(toHash)
    }

    /**
     * Checks if [word] is a tuti.
     * @author Arnau Mora
     * @since 20220308
     * @param word The word to check.
     */
    fun isTuti(word: String): Boolean {
        val letters = letters.value
        var containsAll = true
        for (letter in letters)
            if (!word.contains(letter, true))
                containsAll = false
        return containsAll
    }
}

fun List<Char>.lettersString(centerLetter: Char): String {
    val builder = StringBuilder()
    for ((l, letter) in withIndex()) {
        if (l == 3)
            builder.append(centerLetter)
        builder.append(letter)
    }
    return builder.toString().uppercase().trim()
}
