package com.arnyminerz.paraulogic.utils

import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Generates a set of non-repeating numbers. Note that [count] must not be greater than
 * [until]-[from], and [from] must be lower than [until].
 * @author Arnau Mora
 * @since 20220323
 * @param count The amount of numbers to generate.
 * @param from The minimum number to get.
 * @param until The maximum number to get.
 */
fun generateNumbers(count: Int, from: Int = 1, until: Int = 69) =
    generateSequence { Random.nextInt(from..until) }
        // Makes sure no numbers get repeated
        .distinct()
        // Take the amount of desired values.
        .take(count)
        // Sort the obtained values
        .sorted()
        // Collect into a set
        .toSet()
