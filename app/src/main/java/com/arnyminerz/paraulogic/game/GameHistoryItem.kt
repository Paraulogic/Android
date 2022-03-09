package com.arnyminerz.paraulogic.game

import java.util.Date

data class GameHistoryItem(
    val date: Date,
    val gameInfo: GameInfo,
)

fun Collection<GameHistoryItem>.minDate(): Long {
    var smallest = Long.MAX_VALUE
    for (item in this)
        if (item.date.time < smallest)
            smallest = item.date.time
    return smallest
}

fun Collection<GameHistoryItem>.maxDate(): Long {
    var largest = Long.MIN_VALUE
    for (item in this)
        if (item.date.time > largest)
            largest = item.date.time
    return largest
}
