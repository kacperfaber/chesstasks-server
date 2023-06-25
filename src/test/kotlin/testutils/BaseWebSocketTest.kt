package testutils

import com.chesstasks.websocket.Command
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.websocket.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val objectMapper = ObjectMapper()

open class BaseWebSocketTest : BaseWebTest() {
    data class WebSocketConnection(val session: DefaultWebSocketSession, val call: HttpClientCall) {
        @OptIn(DelicateCoroutinesApi::class)
        suspend fun getCommand(name: String): Command {
            return suspendCoroutine {
                val job = GlobalScope.launch {
                    for (frame in session.incoming) {
                        if (frame !is Frame.Text) continue
                        val text = frame.readText()
                        val command = objectMapper.readValue<Command>(text)
                        if (command.name == name) {
                            it.resume(command)
                        }
                    }
                }
            }
        }
    }

    protected suspend fun webSocket(httpRequestBuilder: HttpRequestBuilder.() -> Unit, act: suspend WebSocketConnection.() -> Unit) {
        val client = app.createClient {
            install(WebSockets) {}
        }

        client.webSocket(httpRequestBuilder) {
            val webSocketConnection = WebSocketConnection(this, call)
            act(webSocketConnection)
        }
    }
}