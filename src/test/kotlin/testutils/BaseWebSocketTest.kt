package testutils

import com.chesstasks.websocket.Command
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val objectMapper = ObjectMapper()

open class BaseWebSocketTest : BaseWebTest() {
    data class WebSocketConnection(val session: DefaultWebSocketSession, val call: HttpClientCall) {
        @OptIn(DelicateCoroutinesApi::class)
        suspend fun getCommand(name: String): Command {
            return suspendCoroutine {
                GlobalScope.launch {
                    for (frame in session.incoming) {
                        if (frame !is Frame.Text) continue
                        val text = frame.readText()
                        val command = objectMapper.readValue<Command>(text)
                        if (command.name == name) {
                            it.resume(command)
                        }
                    }
                    this.cancel()
                }
            }
        }

        suspend fun isProtocolError(){
            val closeReason = session.closeReason.await()
            assertEquals(CloseReason.Codes.PROTOCOL_ERROR, closeReason?.knownReason)
        }

        fun isNotClosed() {
            assertTrue(session.isActive)
        }
    }

    protected suspend fun webSocket(
        httpRequestBuilder: HttpRequestBuilder.() -> Unit,
        act: suspend WebSocketConnection.() -> Unit
    ) {
        val client = app.createClient {
            install(WebSockets) {}
        }

        client.webSocket(httpRequestBuilder) {
            val webSocketConnection = WebSocketConnection(this, call)
            act(webSocketConnection)
        }
    }

    protected suspend fun playEndpoint(userId: Int? = null, act: suspend WebSocketConnection.() -> Unit) {
        val httpRequestBuilder: HttpRequestBuilder.() -> Unit = {
            userId?.let { withToken(userId) };
            url.set(path = "/play")
        }

        webSocket(httpRequestBuilder, act)
    }
}