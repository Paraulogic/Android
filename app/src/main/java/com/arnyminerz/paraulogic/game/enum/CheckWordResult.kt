package com.arnyminerz.paraulogic.game.enum

import androidx.annotation.IntDef

const val CHECK_WORD_CORRECT = 0

const val CHECK_WORD_SHORT = 1

const val CHECK_WORD_CENTER_MISSING = 2

const val CHECK_WORD_INCORRECT = 3

const val CHECK_WORD_ALREADY_FOUND = 4

@IntDef(
    CHECK_WORD_CORRECT,
    CHECK_WORD_SHORT,
    CHECK_WORD_CENTER_MISSING,
    CHECK_WORD_INCORRECT,
    CHECK_WORD_ALREADY_FOUND
)
annotation class CheckWordResult
