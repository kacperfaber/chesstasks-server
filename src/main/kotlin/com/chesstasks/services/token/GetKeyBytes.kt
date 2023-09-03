package com.chesstasks.services.token

import java.nio.charset.Charset
import java.security.MessageDigest

fun String.getKeyBytes(): ByteArray {
    val sha = MessageDigest.getInstance("SHA-256")
    val keyBytes = sha.digest(this.toByteArray(Charset.defaultCharset()))
    return keyBytes.copyOf(16)
}