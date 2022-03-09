package com.arnyminerz.paraulogic.utils

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

fun getMaxDifferenceBetweenDates(dates: List<Date>): Int {
    var maxLen = 1
    for (i in dates.indices) {
        var mn = dates[i]
        var mx = dates[i]
        for (j in i + 1 until dates.size) {
            if (mn > dates[j])
                mn = dates[j]
            if (mx < dates[j])
                mx = dates[j]
            if (daysBetweenDates(mn, mx) == j - i)
                if (maxLen < daysBetweenDates(mn, mx) + 1)
                    maxLen = daysBetweenDates(mn, mx) + 1
        }
    }
    return maxLen
}

fun daysBetweenDates(min: Date, max: Date): Int {
    val mn = Calendar.getInstance().apply { time = min }
    val mx = Calendar.getInstance().apply { time = max }

    val msDiff = mx.timeInMillis - mn.timeInMillis
    return TimeUnit.MILLISECONDS.toDays(msDiff).toInt()
}
