package com.arnyminerz.paraulogic.ui.elements.listeners

import androidx.annotation.IntRange
import com.arnyminerz.paraulogic.ui.elements.ButtonsBox

/**
 * Used as a click listener for the buttons of [ButtonsBox].
 * @author Arnau Mora
 * @since 20220307
 */
interface HexButtonClickListener {
    fun onClick(@IntRange(from = 0L, to = 6L) index: Int, letter: Char)
}
