package com.chesstasks.data

import com.chesstasks.Profiles
import com.chesstasks.data.dao.UserPreferences
import com.chesstasks.data.dao.UserPuzzleHistoryVisibility
import com.chesstasks.data.dao.UserStatisticsVisibility
import com.chesstasks.data.dto.*
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

        Admins.insert {
            it[id] = 0
            it[userId] = 0
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

        TrainingRankings.insert {
            it[userId] = 0
            it[ranking] = 1500
        }

        TrainingRankings.insert {
            it[userId] = 1
            it[ranking] = 1500
        }

        TrainingRankings.insert {
            it[userId] = 2
            it[ranking] = 1500
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
