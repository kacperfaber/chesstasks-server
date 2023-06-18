package com.chesstasks.websocket

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class Command(@SerializedName("n") val name: String, @SerializedName("d") val data: Any?) {
    val g = Gson() // TODO: Using Koin doesn't work

    inline fun <reified T> receive(): T {
        return g.fromJson(g.toJson(data), T::class.java)
    }

    fun <T> receive(cl: Class<T>): T {
        return g.fromJson(g.toJson(data), cl)
    }
}