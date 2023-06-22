package com.chesstasks

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import java.io.File
import kotlin.reflect.KProperty

object Properties {
    private lateinit var properties: DocumentContext

    init {
        val file = File("properties.${Profiles.profile.value.lowercase()}.json")
        if (file.exists()) {
            val json = String(file.readBytes())

            val config = Configuration.defaultConfiguration().setOptions(Option.SUPPRESS_EXCEPTIONS)
            properties = JsonPath.parse(json, config)
        }
    }

    fun <T> get(path: String): T {
        return properties.read(path)
    }

    class PropertiesDelegate<T>(private val path: String) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return get(path)
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            throw Exception("Cannot assign PropertiesDelegate property")
        }
    }

    fun <T> value(path: String): PropertiesDelegate<T> = PropertiesDelegate(path)
}