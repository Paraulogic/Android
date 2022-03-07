package com.arnyminerz.paraulogic.crypto

import java.math.BigInteger
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

/**
 * Hashes the [input] with MD5.
 * @author Arnau Mora
 * @since 20220307
 * @param input The string to hash.
 */
fun md5(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray(UTF_8)))
        .toString(16)
        .padStart(32, '0')
}
