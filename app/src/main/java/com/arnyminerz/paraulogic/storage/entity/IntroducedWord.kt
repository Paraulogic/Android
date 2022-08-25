package com.arnyminerz.paraulogic.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.arnyminerz.paraulogic.game.GameInfo
import com.arnyminerz.paraulogic.utils.append
import com.arnyminerz.paraulogic.utils.startOfDay
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

/**
 * Represents a word the user has typed and that has been stored.
 * @author Arnau Mora
 * @since 20220825
 */
@Entity
data class IntroducedWord(
    /**
     * The uid of the register in the database.
     * @author Arnau Mora
     * @since 20220825
     */
    @PrimaryKey(autoGenerate = true) val uid: Int,
    /**
     * The instant when the word was registered.
     * @author Arnau Mora
     * @since 20220825
     */
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    /**
     * The hash of the GameInfo of the day the word was introduced. This is used because there will
     * definitely be different days with matching words.
     * @author Arnau Mora
     * @since 20220825
     */
    @ColumnInfo(name = "hash") val hash: String,
    /**
     * The word registered.
     * @author Arnau Mora
     * @since 20220825
     */
    @ColumnInfo(name = "word") val word: String,
    /**
     * Whether or not the word was correct. This is used for storing also the words the user
     * mistyped, and therefore informing on how many words were made up.
     * @author Arnau Mora
     * @since 20220825
     */
    @ColumnInfo(name = "isCorrect") val isCorrect: Boolean,
) {
    constructor(jsonObject: JSONObject) : this(
        jsonObject.getInt("uid"),
        jsonObject.getLong("timestamp"),
        jsonObject.getString("hash"),
        jsonObject.getString("word"),
        jsonObject.getBoolean("isCorrect"),
    )

    constructor(array: JSONArray) : this(
        array.getInt(0),
        array.getLong(1),
        array.getString(2),
        array.getString(3),
        when (val correct = array.get(4)) {
            is Int -> correct == 1
            is Boolean -> correct
            else -> correct.toString().toBoolean() // Won't get called, but keep just in case
        },
    )

    /**
     * Parses [timestamp] as a [Date].
     * @author Arnau Mora
     * @since 20220825
     */
    @Ignore
    val date: Date = Date(timestamp)

    override fun hashCode(): Int =
        uid.hashCode() + timestamp.hashCode() + hash.hashCode() + word.hashCode() + isCorrect.hashCode()

    /**
     * Provides a simplified representation of the data in the class as a JSON array, using positional
     * values instead of key-based values. This saves a lot of space in large sets of data.
     * @author Arnau Mora
     * @since 20220825
     */
    fun simplifiedJson(): JSONArray = JSONArray().apply {
        put(uid)
        put(timestamp)
        put(hash)
        put(word)
        put(if (isCorrect) 1 else 0)
    }

    fun jsonObject(): JSONObject =
        JSONObject().apply {
            put("uid", uid)
            put("timestamp", timestamp)
            put("hash", hash)
            put("word", word)
            put("isCorrect", isCorrect)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntroducedWord

        if (uid != other.uid) return false
        if (timestamp != other.timestamp) return false
        if (hash != other.hash) return false
        if (word != other.word) return false
        if (isCorrect != other.isCorrect) return false

        return true
    }
}

/**
 * Divides the collection of words into each day.
 * @author Arnau Mora
 * @since 20220825
 */
fun Collection<IntroducedWord>.byDays(): Map<Date, List<IntroducedWord>> {
    val map = hashMapOf<Date, ArrayList<IntroducedWord>>()

    for (word in this) {
        val startOfDay = word.date.startOfDay()
        map[startOfDay] = map[startOfDay]?.append(word) ?: arrayListOf(word)
    }

    return map
}

/**
 * Associates each [GameInfo] with a list of words: the words introduced the day of each [GameInfo].
 * @author Arnau Mora
 * @since 20220825
 */
fun Pair<List<GameInfo>, List<IntroducedWord>>.associate(): Map<Date, Pair<GameInfo, List<IntroducedWord>>> {
    val map = hashMapOf<Date, Pair<GameInfo, List<IntroducedWord>>>()

    for (gameInfo in first) {
        val startOfDay = gameInfo.timestamp.startOfDay()
        val words = second.filter { it.hash == gameInfo.hash }
        map[startOfDay] = gameInfo to words
    }

    return map
}
