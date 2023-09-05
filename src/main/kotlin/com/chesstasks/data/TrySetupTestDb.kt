package com.chesstasks.data

import com.chesstasks.Constants
import com.chesstasks.Profiles
import com.chesstasks.data.dao.UserPreferences
import com.chesstasks.data.dao.UserPuzzleHistoryVisibility
import com.chesstasks.data.dao.UserStatisticsVisibility
import com.chesstasks.data.dto.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

private fun setupUserDefaults(vararg userIds: Int) {
    userIds.forEach { id ->
        TrainingRankings.insert {
            it[userId] = id
            it[ranking] = 1500
        }

        UserPreferences.insert {
            it[UserPreferences.id] = id
            it[userId] = id
            it[historyVisibility] = UserPuzzleHistoryVisibility.ME
            it[statisticsVisibility] = UserStatisticsVisibility.ME
        }
    }
}

fun Application.trySetupTestDb() = transaction {
    if (!Profiles.isProd() && System.getProperty(Constants.TestingInitDbVar).toBoolean()) {
        Users.insert {
            it[id] = 0
            it[username] = "admin1"
            it[passwordHash] = "HelloWorld123"
            it[emailAddress] = "admin1@gmail.com"
        }

        Admins.insert {
            it[id] = 0
            it[userId] = 0
        }

        Users.insert {
            it[id] = 1
            it[username] = "user2"
            it[emailAddress] = "user2@gmail.com"
            it[passwordHash] = "HelloWorld123"
        }

        Users.insert {
            it[id] = 2
            it[username] = "user3"
            it[emailAddress] = "user3@gmail.com"
            it[passwordHash] = "HelloWorld123"
        }

        /* Make sure to setup all users */
        setupUserDefaults(0, 1, 2)

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
            it[fen] = "k6r/8/8/8/2Q5/8/8/K7 b - - 0 1"
            it[moves] = "h8e8 c4c6 a8b8 c6e8"
            it[ranking] = 1400
            it[database] = PuzzleDatabase.LICHESS
            // themes = mateIn1, long
        }

        Puzzles.insert {
            it[id] = 1
            it[fen] = "k7/1q6/1b6/8/6B1/8/4K3/8 b - - 0 1"
            it[moves] = "b6a5 g4f3 b7f3 e2f3"
            it[ranking] = 1400
            it[database] = PuzzleDatabase.LICHESS
            // themes = mateIn2
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

        Themes.insert {
            it[id] = 0
            it[name] = "mateIn1"
        }

        Themes.insert {
            it[id] = 1
            it[name] = "fast"
        }

        Themes.insert {
            it[id] = 2
            it[name] = "mateIn2"
        }

        PuzzleThemes.insert {
            it[puzzleId] = 0
            it[themeId] = 0 // mateIn1
        }

        PuzzleThemes.insert {
            it[puzzleId] = 0
            it[themeId] = 1 // fast
        }

        PuzzleThemes.insert {
            it[puzzleId] = 1
            it[themeId] = 2 // mateIN2
        }
    }
}
