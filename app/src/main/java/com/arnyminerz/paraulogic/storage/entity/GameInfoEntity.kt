package com.arnyminerz.paraulogic.storage.entity

import androidx.compose.runtime.mutableStateOf
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.paraulogic.game.GameInfo

@Entity(tableName = "GameInfoList")
class GameInfoEntity(
    @PrimaryKey val date: Long,
    val letters: String,
    val centerLetter: Char,
    val words: Map<String, String>,
) {
    companion object {
        fun fromGameInfo(date: Long, gameInfo: GameInfo) =
            GameInfoEntity(
                date,
                gameInfo.letters.value.joinToString(""),
                gameInfo.centerLetter,
                gameInfo.words,
            )
    }

    val gameInfo: GameInfo
        get() = GameInfo(mutableStateOf(letters.toCharArray().toList()), centerLetter, words)
}
