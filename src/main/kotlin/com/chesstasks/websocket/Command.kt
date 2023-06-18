package com.chesstasks.websocket

import com.google.gson.Gson
import org.koin.java.KoinJavaComponent

class Command(val name: String, val data: String?) {
    val gson by KoinJavaComponent.inject<Gson>(Gson::class.java)

    inline fun <reified T> receive(): T {
        return gson.fromJson(data, T::class.java)
    }

    fun <T> receive(cl: Class<T>): T {
        return gson.fromJson(data, cl)
    }
}