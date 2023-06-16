package com.chesstasks.data

import com.chesstasks.data.dao.UserPreferences
import com.chesstasks.data.dto.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

object DatabaseFactory {
    private val tables = listOf(
        Users,
        Tokens,
        Admins,
        Puzzles,
        PuzzleHistoryItems,
        UserPreferences,
        Friends,
        FriendRequests,
        PuzzleThemes,
        Themes
    ).toTypedArray()

    fun init() {
        // TODO: No 'production' database.
        val jdbc = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=TRUE"
        val driverClassName = "org.h2.Driver"
        val db = Database.connect(jdbc, driverClassName, user = "sa", password = "sa")

        transaction(db) {
            SchemaUtils.create(*tables)

            commit()
        }
    }

    fun init(dataSource: DataSource) {
        val db = Database.connect(dataSource)

        transaction(db) {
            SchemaUtils.create(*tables)
            commit()
        }
    }

    fun dropTables() {
        transaction {
            SchemaUtils.drop(*tables)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}