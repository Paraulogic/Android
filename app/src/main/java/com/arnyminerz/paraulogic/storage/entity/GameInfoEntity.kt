package com.arnyminerz.paraulogic.storage.entity

import androidx.compose.runtime.mutableStateOf
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.paraulogic.game.GameInfo
import java.util.Date

@Entity(tableName = "GameInfoList")
class GameInfoEntity(
    @PrimaryKey val date: Long,
    val letters: String,
    val centerLetter: Char,
    val words: Map<String, String>,
) {
    companion object {
        fun fromGameInfo(gameInfo: GameInfo) =
            GameInfoEntity(
                gameInfo.timestamp.time,
                gameInfo.letters.value.joinToString(""),
                gameInfo.centerLetter,
                gameInfo.words,
            )
    }

    val gameInfo: GameInfo
        get() = GameInfo(
            Date(date),
            mutableStateOf(letters.toCharArray().toList()),
            centerLetter,
            words,
        )
}
