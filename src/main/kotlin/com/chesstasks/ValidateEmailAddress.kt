package com.chesstasks

fun String.validateEmailAddress(): Boolean {
    val reg = "[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+".toRegex()
    return reg.matches(this)
}