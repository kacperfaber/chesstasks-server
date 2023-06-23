package com.chesstasks.websocket

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.Gson

class Command(@JsonProperty("n") val name: String, @JsonProperty("d") val data: Any?) {
    val g = Gson() // TODO: Using Koin doesn't work

    inline fun <reified T> receive(): T {
        return g.fromJson(g.toJson(data), T::class.java)
    }

    fun <T> receive(cl: Class<T>): T {
        return g.fromJson(g.toJson(data), cl)
    }
}