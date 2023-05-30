package testutils

import com.chesstasks.data.DatabaseFactory
import com.chesstasks.module
import com.google.gson.Gson
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.GlobalContext.get

// TODO: Should I create custom test runner with annotations?

open class BaseTest {
    open lateinit var app: TestApplication

    protected inline fun <reified T> getInstance(): T = get().get(T::class)

    @BeforeEach
    fun setup()  {
        app = TestApplication { application { module() } }
        app.start()
    }

    @AfterEach
    fun teardown() {
        app.stop()
        DatabaseFactory.dropTables()
    }
}

open class BaseWebTest : BaseTest() {
    protected fun HttpRequestBuilder.jsonBody(vararg pairs: Pair<Any, Any>) {
        contentType(ContentType.Application.Json)
        setBody(Gson().toJson(mapOf(*pairs)))
    }
}