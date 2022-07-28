package com.arnyminerz.paraulogic.utils

import org.json.JSONArray
import org.json.JSONObject

fun String.toJsonArray(): JSONArray = JSONArray(this)

fun <T> JSONArray.mapJsonObject(constructor: (json: JSONObject) -> T): List<T> =
    arrayListOf<T>().apply {
        for (i in 0 until length())
            add(constructor(getJSONObject(i)))
    }

fun <V, T> JSONArray.map(constructor: (item: V) -> T): List<T> =
    arrayListOf<T>().apply {
        @Suppress("UNCHECKED_CAST")
        for (i in 0 until length())
            add(constructor(this@map.get(i) as V))
    }

fun JSONObject.toMap(): Map<String, Any> =
    keys()
        .asSequence()
        .map { it to get(it) }
        .toMap()
