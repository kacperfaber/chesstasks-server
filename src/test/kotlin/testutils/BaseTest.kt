package testutils

import com.chesstasks.Profile
import com.chesstasks.Profiles
import com.chesstasks.data.DatabaseFactory
import com.chesstasks.data.dto.Tokens
import com.chesstasks.module
import com.chesstasks.services.token.Token
import com.google.gson.Gson
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.GlobalContext.get
import java.util.*
import kotlin.test.assertEquals

// TODO: Should I create custom test runner with annotations?

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Inject

open class BaseTest {
    open lateinit var app: TestApplication

    protected inline fun <reified T> getInstance(): T = get().get(T::class)

    protected fun getInstance(cl: Class<*>): Any = get().get(cl.kotlin)

    @BeforeEach
    fun setup() {
        app = TestApplication {
            application {
                Profiles.profileFallback = Profile.TEST
                module()
            }
        }
        app.start()

        this.javaClass.fields
            .filter { it.isAnnotationPresent(Inject::class.java) }
            .map { field -> field.set(this, getInstance(field.type)) }
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

    protected fun HttpRequestBuilder.withToken(userId: Int, useSecret: String? = null) {
        val secret = useSecret ?: UUID.randomUUID().toString()

        transaction {
            Tokens.insert {
                it[id] = 0
                it[Tokens.userId] = userId
                it[createdAt] = System.currentTimeMillis()
                it[Tokens.secret] = secret
            }
        }

        header("Authorization", Gson().toJson(Token(userId, secret)))
    }

    protected suspend inline fun <reified T> HttpResponse.fromJson(): T {
        return Gson().fromJson(bodyAsText(), T::class.java)
    }
}