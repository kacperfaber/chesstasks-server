package com.chesstasks.data

import com.chesstasks.data.dto.Tokens
import com.chesstasks.data.dto.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        // TODO: No 'production' database.
        val jdbc = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=TRUE"
        val driverClassName = "org.h2.Driver"
        val db = Database.connect(jdbc, driverClassName, user = "sa", password = "sa")

        transaction(db) {
            SchemaUtils.create(Users, Tokens)
            commit()
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}