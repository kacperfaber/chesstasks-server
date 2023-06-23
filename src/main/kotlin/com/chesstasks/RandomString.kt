package com.chesstasks

import kotlin.random.Random

private val chars = ('a'..'a') + ('A'..'Z') + ('0'..'9')


fun randomString(size: Int): String {
    return buildString {
        repeat(size) {
            val i = Random.nextInt(0, size)
            append(chars[i])
        }
    }
}