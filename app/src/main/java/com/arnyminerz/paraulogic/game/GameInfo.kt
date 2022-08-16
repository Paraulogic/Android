package com.arnyminerz.paraulogic.game

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.arnyminerz.paraulogic.crypto.md5
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_ALREADY_FOUND
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_CENTER_MISSING
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_CORRECT
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_INCORRECT
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_SHORT
import com.arnyminerz.paraulogic.game.annotation.CheckWordResult
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.utils.generateNumbers
import com.arnyminerz.paraulogic.utils.map
import com.arnyminerz.paraulogic.utils.toMap
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The amount of levels there are.
 * @author Arnau Mora
 * @since 20220308
 */
const val AMOUNT_OF_LEVELS = 7

data class GameInfo(
    val timestamp: Date,
    var letters: MutableState<List<Char>>,
    val centerLetter: Char,
    val words: Map<String, String>,
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: JSONObject): GameInfo {
            val timestamp = json
                .takeIf { it.has("timestamp") }
                ?.getString("timestamp")
                ?.let {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(
                        it
                    )
                }
                ?: throw NoSuchElementException("Could not find \"timestamp\" in response data.")

            val gameInfo = json.getJSONObject("game_info")
                ?: throw NoSuchElementException("Could not find \"game_info\" in response data.")
            val centerLetter = gameInfo
                .takeIf { it.has("center_letter") }
                ?.getString("center_letter")
                ?.takeIf { it.isNotBlank() }
                ?.get(0)
                ?: throw NoSuchElementException("Could not find \"centerLetter\" in response data.")
            val letters = gameInfo
                .takeIf { it.has("letters") }
                ?.getJSONArray("letters")
                ?.takeIf { it.length() > 0 }
                ?.map<String, Char> { it[0] }
                ?: throw NoSuchElementException("Could not find \"letters\" in response data.")
            val words = gameInfo
                .takeIf { it.has("words") }
                ?.getJSONObject("words")
                ?.toMap()
                ?.mapValues { it.toString() }
                ?: throw NoSuchElementException("Could not find \"words\" in response data.")

            return GameInfo(timestamp, mutableStateOf(letters), centerLetter, words)
        }

        /**
         * Used by [random] by default to generate words. Generates random words, may not have any
         * sense.
         * @author Arnau Mora
         * @since 20220323
         */
        private val randomWordGenerator: (letters: Collection<Char>, wordsCount: Int, minWordLength: Int, maxWordLength: Int) -> List<String> =
            { letters, wordsCount, minWordLength, maxWordLength ->
                val wordsList = arrayListOf<String>()
                for (c in 0 until wordsCount)
                    List(
                        // Get a random number between minWordLength and maxWordLength, this will be the
                        // length of the word.
                        (minWordLength..maxWordLength).random()
                    ) {
                        // Select random letters from the letters list
                        letters.random()
                    } // Stick all the letters together into a string
                        .joinToString()
                wordsList
            }

        /**
         * Generates a random [GameInfo] instance.
         * @author Arnau Mora
         * @since 20220323
         * @param wordsCount The amount of words to generate.
         * @param minWordLength The minimum length of each word.
         * @param maxWordLength The maximum length of each word.
         * @param generator A function that generates words from the given parameters.
         */
        @Suppress("SpellCheckingInspection")
        fun random(
            wordsCount: Int = 40,
            minWordLength: Int = 3,
            maxWordLength: Int = 10,
            generator: (letters: Collection<Char>, wordsCount: Int, minWordLength: Int, maxWordLength: Int) -> List<String> = randomWordGenerator
        ): GameInfo {
            // First, generate letters
            val lettersSource = ('A'..'Z') + 'Ç'
            val indexes = generateNumbers(7, from = 0, until = lettersSource.size)
            val letters = arrayListOf<Char>()
            for (index in indexes)
                letters.add(lettersSource[index])

            // Now, generate words
            val wordsList = generator(letters, wordsCount, minWordLength, maxWordLength)

            // Generate a random date
            val timestamp = Date(com.arnyminerz.paraulogic.utils.random(0, Int.MAX_VALUE).toLong())

            return GameInfo(
                timestamp,
                mutableStateOf(letters),
                letters[5],
                wordsList.associateWith { it })
        }
    }

    /**
     * Calculates the maximum amount of points that can be obtained in the game.
     * @author Arnau Mora
     * @since 20220308
     */
    val maxPoints: Int

    /**
     * Returns the amount of tutis there are on the game info.
     * @author Arnau Mora
     * @since 20220308
     */
    val tutisCount: Int

    /**
     * Returns all the tutis there are in the [GameInfo].
     * @author Arnau Mora
     * @since 20220309
     */
    val tutis: List<String>

    /**
     * Returns the amount of points that each level has.
     * @author Arnau Mora
     * @since 20220308
     */
    val pointsPerLevel: Int

    /**
     * Returns a unique hash of the game info.
     * @author Arnau Mora
     * @since 20220307
     */
    val hash: String

    init {
        var protoMaxPoints = 0
        for (word in words.keys)
            protoMaxPoints += when (word.length) {
                3 -> 1
                4 -> 2
                else -> word.length + (if (isTuti(word)) 10 else 0)
            }
        maxPoints = protoMaxPoints

        val tutisList = arrayListOf<String>()
        for (word in words.keys)
            if (isTuti(word))
                tutisList.add(word)
        tutis = tutisList
        tutisCount = tutisList.size

        pointsPerLevel = maxPoints / AMOUNT_OF_LEVELS

        // Make sure that there are 6 letters (+ the center one)
        if (letters.value.size < 6)
            letters.value = letters
                .value
                .toMutableList()
                .apply {
                    (letters.value.size until 6)
                        .forEach { _ -> add('\u0000') }
                }

        val sortedLetters = letters.value.sorted()
        val sortedWords = words.entries.sortedBy { it.key }.map { it.key + ":" + it.value }
        val toHash = sortedLetters.joinToString(";") +
                sortedWords.joinToString(";") +
                centerLetter
        hash = md5(toHash)
    }

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
     * Checks if [word] is a tuti.
     * @author Arnau Mora
     * @since 20220308
     * @param word The word to check.
     */
    fun isTuti(word: String): Boolean {
        val letters = letters.value
        for (letter in letters)
            if (letter != '\u0000' && !word.contains(letter, true))
                return false
        return true
    }

    /**
     * Checks the validity of a word.
     * @author Arnau Mora
     * @since 20220308
     * @param word The word to check.
     * @param foundWords The words that the user has found until now.
     */
    @CheckWordResult
    fun checkWord(word: String, foundWords: List<IntroducedWord>): Int {
        val answer = word.trim().lowercase()
        return if (answer.length < 3)
            CHECK_WORD_SHORT
        else if (!answer.contains(centerLetter))
            CHECK_WORD_CENTER_MISSING
        else if (!words.contains(answer))
            CHECK_WORD_INCORRECT
        else if (foundWords.map { it.word.lowercase() }.contains(answer))
            CHECK_WORD_ALREADY_FOUND
        else
            CHECK_WORD_CORRECT
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

fun Collection<GameInfo>.minDate(): Long {
    var smallest = Long.MAX_VALUE
    for (item in this)
        if (item.timestamp.time < smallest)
            smallest = item.timestamp.time
    return smallest
}

fun Collection<GameInfo>.maxDate(): Long {
    var largest = Long.MIN_VALUE
    for (item in this)
        if (item.timestamp.time > largest)
            largest = item.timestamp.time
    return largest
}
