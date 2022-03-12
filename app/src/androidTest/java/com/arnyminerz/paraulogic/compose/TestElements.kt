package com.arnyminerz.paraulogic.compose

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arnyminerz.paraulogic.ui.elements.ButtonsBox
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestElements {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testButtonsBox() {
        val letters = "ABCDEFG"
        var text = ""

        composeTestRule.setContent {
            ButtonsBox(letters = "ABCDEFG", onClick = { _, letter -> text += letter })
        }

        for ((i, letter) in letters.withIndex()) {
            composeTestRule.onNodeWithText(letter.toString()).performClick()
            assert(text == text.substring(0, i + 1))
        }
    }
}