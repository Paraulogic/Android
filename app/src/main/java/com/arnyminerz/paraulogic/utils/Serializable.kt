package com.arnyminerz.paraulogic.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInput
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * Converts a byte array into [T].
 * @author Arnau Mora
 * @since 20220317
 */
@Suppress("UNCHECKED_CAST")
fun <T : Serializable> ByteArray.fromByteArray(): T {
    val byteArrayInputStream = ByteArrayInputStream(this)
    val objectInput: ObjectInput
    objectInput = ObjectInputStream(byteArrayInputStream)
    val result = objectInput.readObject() as T
    objectInput.close()
    byteArrayInputStream.close()
    return result
}

/**
 * Converts a Serializable into a ByteArray.
 * @author Arnau Mora
 * @since 20220317
 */
fun Serializable.toByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
    objectOutputStream.writeObject(this)
    objectOutputStream.flush()
    val result = byteArrayOutputStream.toByteArray()
    byteArrayOutputStream.close()
    objectOutputStream.close()
    return result
}
