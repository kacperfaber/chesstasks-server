package com.chesstasks.data

import com.chesstasks.Profiles
import com.chesstasks.data.dto.Friends
import com.chesstasks.data.dto.Users
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.trySetupTestDb() = transaction {
    if (!Profiles.isProd() && System.getProperty("testing.initdb").toBoolean()) {
        Users.insert {
            it[id] = 0
            it[username] = "kacperfaber"
            it[passwordHash] = "HelloWorld123"
            it[emailAddress] = "kacperf1234@gmail.com"
        }

        Users.insert {
            it[id] = 1
            it[username] = "Aneta"
            it[emailAddress] = "anetka@gmail.com"
            it[passwordHash] = "HelloWorld123"
        }

        Users.insert {
            it[id] = 2
            it[username] = "Kamil"
            it[emailAddress] = "kamil@gmail.com"
            it[passwordHash] = "HelloWorld123"
        }

        Friends.insert {
            it[id] = 0
            it[userId] = 0
            it[secondUserId] = 1
        }
    }
}