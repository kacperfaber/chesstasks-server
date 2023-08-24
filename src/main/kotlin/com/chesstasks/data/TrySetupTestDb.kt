package com.chesstasks.data

import com.chesstasks.Profiles
import com.chesstasks.data.dao.UserPreferences
import com.chesstasks.data.dao.UserPuzzleHistoryVisibility
import com.chesstasks.data.dao.UserStatisticsVisibility
import com.chesstasks.data.dto.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
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

        UserPreferences.insert {
            it[id] = 0
            it[userId] = 0
            it[historyVisibility] = UserPuzzleHistoryVisibility.ME
            it[statisticsVisibility] = UserStatisticsVisibility.ME
        }

        UserPreferences.insert {
            it[id] = 1
            it[userId] = 1
            it[historyVisibility] = UserPuzzleHistoryVisibility.ONLY_FRIENDS
            it[statisticsVisibility] = UserStatisticsVisibility.ONLY_FRIENDS
        }

        UserPreferences.insert {
            it[id] = 2
            it[userId] = 2
            it[historyVisibility] = UserPuzzleHistoryVisibility.EVERYONE
            it[statisticsVisibility] = UserStatisticsVisibility.EVERYONE
        }

        Friends.insert {
            it[id] = 0
            it[userId] = 0
            it[secondUserId] = 1
        }
		
		FriendRequests.insert {
			it[id] = 0
			it[senderId] = 0
			it[targetId] = 2
		}

        Puzzles.insert {
            it[id] = 0
            it[fen] = "k3r3/8/8/8/2Q5/7R/8/K7 w - - 0 1"
            it[moves] = "h8e8 c4c6 a8b8 c6e8"
            it[ranking] = 1400
            it[database] = PuzzleDatabase.LICHESS
        }

        PuzzleHistoryItems.insert {
            it[id] = 0
            it[puzzleId] = 0
            it[userId] = 0
            it[moves] = "h8e8 c4c6 a8b8 c6c5"
            it[success] = false
        }

        PuzzleHistoryItems.insert {
            it[id] = 1
            it[puzzleId] = 0
            it[userId] = 0
            it[moves] = "h8e8 c4c6 a8b8 c6e8"
            it[success] = true
        }
    }
}