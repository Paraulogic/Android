package com.arnyminerz.paraulogic.utils

/**
 * Adds [item] to the collection, and returns self.
 * @author Arnau Mora
 * @since 20220825
 */
fun <T, C : MutableCollection<T>> C.append(item: T): C {
    add(item)
    return this
}
