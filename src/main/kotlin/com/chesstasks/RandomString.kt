package com.chesstasks

import kotlin.random.Random

private val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')


fun randomString(size: Int): String {
    return buildString {
        repeat(size) {
            val i = Random.nextInt(0, chars.size)
            append(chars[i])
        }
    }
}