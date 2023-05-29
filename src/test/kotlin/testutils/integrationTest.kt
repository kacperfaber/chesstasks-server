package testutils

import com.chesstasks.data.DatabaseFactory
import com.chesstasks.module
import io.kotest.assertions.asClue
import io.ktor.server.testing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow

@OptIn(DelicateCoroutinesApi::class)
fun integrationTest(act: suspend ApplicationTestBuilder.() -> Unit) {
    testApplication {
        application {
            val coroutine = GlobalScope.launch {
                module()
                act(this@testApplication)
            }

            runBlocking {
                coroutine.join()
            }
        }


    }
}