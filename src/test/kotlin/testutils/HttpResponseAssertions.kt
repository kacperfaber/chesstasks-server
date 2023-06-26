package testutils

import com.google.gson.Gson
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.test.assertEquals

fun HttpStatusCode.isOk() {
    assertEquals(HttpStatusCode.OK, this)
}

fun HttpStatusCode.isBadRequest() {
    assertEquals(HttpStatusCode.BadRequest, this)
}

fun HttpStatusCode.isForbid() {
    assertEquals(HttpStatusCode.Forbidden, this)
}

fun HttpStatusCode.isUnauthorized() {
    assertEquals(HttpStatusCode.Unauthorized, this)
}

fun HttpStatusCode.isNoContent() {
    assertEquals(HttpStatusCode.NoContent, this)
}

fun HttpStatusCode.isUnsupportedMediaType() {
    assertEquals(HttpStatusCode.UnsupportedMediaType, this)
}

fun <T> String.jsonPath(path: String): T? {
    val config = Configuration.defaultConfiguration().setOptions(Option.SUPPRESS_EXCEPTIONS)
    val parsed = JsonPath.parse(this, config)
    return parsed.read<T?>(path)
}

suspend fun <T> HttpResponse.jsonPath(path: String): T? {
    val config = Configuration.defaultConfiguration().setOptions(Option.SUPPRESS_EXCEPTIONS)
    val parsed = JsonPath.parse(bodyAsText(), config)
    return parsed.read<T?>(path)
}

suspend fun <T> HttpResponse.jsonPath(path: String, expected: T?): T? {
    val config = Configuration.defaultConfiguration().setOptions(Option.SUPPRESS_EXCEPTIONS)
    val parsed = JsonPath.parse(bodyAsText(), config)
    val value = parsed.read<T?>(path)
    assertEquals(expected, value)
    return value
}

suspend inline fun <reified T> HttpResponse.bodyAs(): T {
    return Gson().fromJson(bodyAsText(), T::class.java)
}