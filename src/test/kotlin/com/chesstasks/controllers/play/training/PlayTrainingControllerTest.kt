package com.chesstasks.controllers.play.training

import com.chesstasks.data.dao.UserPreferences
import com.chesstasks.data.dao.UserPuzzleHistoryVisibility
import com.chesstasks.data.dao.UserStatisticsVisibility
import com.chesstasks.data.dto.*
import io.ktor.client.request.*
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import testutils.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class PlayTrainingControllerTest : BaseWebTest() {
    private fun setupUser() = transaction {
        Users.insert {
            it[id] = 0
            it[emailAddress] = "kacperf1234@gmail.com"
            it[username] = "kacperfaber"
            it[passwordHash] = "HelloWorld123"
        }
    }

    private fun setupAdmin() = transaction {
        Admins.insert {
            it[id] = 0
            it[userId] = 0
        }
    }

    companion object {
        const val initialFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }

    private fun setupRandomPuzzles(iter: Int, database: PuzzleDatabase = PuzzleDatabase.LICHESS, ranking: Int = 1500) {
        return transaction {
            repeat(iter) {
                Puzzles.insert {
                    it[fen] = initialFEN
                    it[moves] = "e2e4 e7e5"
                    it[Puzzles.database] = database
                    it[Puzzles.ranking] = ranking
                }
            }
        }
    }

    @Test
    fun `puzzlesEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.post("/api/play/training/puzzles").status.isForbid()
    }

    @Test
    fun `puzzlesEndpoint returns 415 if authenticated as user and NO BODY`() = testSuspend {
        setupUser()
        app.client.post("/api/play/training/puzzles") { withToken(0) }.status.isUnsupportedMediaType()
    }

    private fun HttpRequestBuilder.getPuzzlePayload(
        rankingOffset: Int? = null,
        themeId: Int? = null,
        database: PuzzleDatabase? = null
    ) {
        jsonBody("rankingOffset" to rankingOffset, "themeId" to themeId, "database" to database)
    }

    @Test
    fun `puzzlesEndpoint returns OK if authenticated as user and BODY`() = testSuspend {
        setupUser()
        app.client.post("/api/play/training/puzzles") { withToken(0); getPuzzlePayload() }.status.isOk()
    }

    @Test
    fun `puzzlesEndpoint returns OK if authenticated as admin and BODY`() = testSuspend {
        setupUser()
        setupAdmin()
        app.client.post("/api/play/training/puzzles") { withToken(0); getPuzzlePayload() }.status.isOk()
    }

    @Test
    fun `puzzlesEndpoint returns OK and expected 50 items length`() = testSuspend {
        setupUser()
        setupRandomPuzzles(500)
        val resp = app.client.post("/api/play/training/puzzles") { withToken(0); getPuzzlePayload() }
        resp.status.isOk()
        resp.jsonPath("$.length()", 50)
    }

    @Test
    fun `puzzlesEndpoint returns OK and random items`() = testSuspend {
        setupUser()
        setupRandomPuzzles(10000, PuzzleDatabase.LICHESS, 1500)

        val secret = "ABC"
        createToken(0, secret)

        val r1 = app.client.post("/api/play/training/puzzles") { useToken(0, secret); getPuzzlePayload() }
        r1.status.isOk()
        val r2 = app.client.post("/api/play/training/puzzles") { useToken(0, secret); getPuzzlePayload() }
        r2.status.isOk()

        val r1FirstId = r1.jsonPath<Int>("$[0].id")
        val r2FirstId = r2.jsonPath<Int>("$[0].id")

        assertNotEquals(r1FirstId, r2FirstId)
    }

    @Test
    fun `puzzlesEndpoint returns OK and only items matching to PuzzleDatabase we want - LICHESS`() = testSuspend {
        setupUser()
        setupRandomPuzzles(25, PuzzleDatabase.LICHESS, 1500)
        setupRandomPuzzles(30, PuzzleDatabase.USER, 1500)

        val r =
            app.client.post("/api/play/training/puzzles") { withToken(0); getPuzzlePayload(database = PuzzleDatabase.LICHESS) }
        r.status.isOk()
        val idsList = r.jsonPath<List<Int>>("$[?(@.database == 'LICHESS')].id")
        assertEquals(25, idsList?.count())
    }

    @Test
    fun `puzzlesEndpoint returns OK and only items matching to PuzzleDatabase we want - USER`() = testSuspend {
        setupUser()
        setupRandomPuzzles(25, PuzzleDatabase.LICHESS, 1500)
        setupRandomPuzzles(30, PuzzleDatabase.USER, 1500)

        val r =
            app.client.post("/api/play/training/puzzles") { withToken(0); getPuzzlePayload(database = PuzzleDatabase.USER) }
        r.status.isOk()
        val idsList = r.jsonPath<List<Int>>("$[?(@.database == 'USER')].id")
        assertEquals(30, idsList?.count())
    }

    @Test
    fun `puzzlesEndpoint returns BAD_REQUEST if rankingOffset is greater than 1000`() = testSuspend {
        setupUser()
        setupRandomPuzzles(25, PuzzleDatabase.LICHESS, 1500)
        setupRandomPuzzles(30, PuzzleDatabase.USER, 1500)

        val r =
            app.client.post("/api/play/training/puzzles") { withToken(0); getPuzzlePayload(database = PuzzleDatabase.USER) }
        r.status.isOk()
        val idsList = r.jsonPath<List<Int>>("$[?(@.database == 'USER')].id")
        assertEquals(30, idsList?.count())
    }

    @Test
    fun `submitPuzzleEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.post("/api/play/training/0/submit").status.isForbid()
    }

    private fun HttpRequestBuilder.submitPayload(success: Boolean? = true, moves: String? = "e2e4") {
        val pairs = listOfNotNull(
            if (success != null) "success" to success else null,
            if (moves != null) "moves" to moves else null
        ).toTypedArray()

        jsonBody(*pairs)
    }

    private fun setupRandomPuzzlesWithId(iter: Int) = transaction {
        repeat(iter) { itId ->
            Puzzles.insert {
                it[id] = itId
                it[fen] = initialFEN
                it[moves] = "e2e4"
                it[database] = PuzzleDatabase.LICHESS
                it[ranking] = 1500
            }
        }
    }

    @Test
    fun `submitPuzzleEndpoint returns 415 if authenticated as user and resource exist but no body`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(10)
        app.client.post("/api/play/training/0/submit") { withToken(0) }.status.isUnsupportedMediaType()
    }

    @Test
    fun `submitPuzzleEndpoint returns BAD_REQUEST if authenticated as user but resource does not exist and body`() =
        testSuspend {
            setupUser()
            app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload() }.status.isBadRequest()
        }

    @Test
    fun `submitPuzzleEndpoint returns BAD_REQUEST if authenticated as user but missing 'success' body parameter`() =
        testSuspend {
            setupUser()
            app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload(success = null) }.status.isBadRequest()
        }

    @Test
    fun `submitPuzzleEndpoint returns BAD_REQUEST if authenticated as user but missing 'moves' body parameter`() =
        testSuspend {
            setupUser()
            app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload(moves = null) }.status.isBadRequest()
        }

    @Test
    fun `submitPuzzleEndpoint returns OK and created PuzzleHistoryItems row`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(100)

        val puzzleHistoryItemBefore = transaction {
            PuzzleHistoryItems.select { PuzzleHistoryItems.puzzleId eq 0 }.map { PuzzleHistoryDto.from(it) }
        }.firstOrNull()
        assertNull(puzzleHistoryItemBefore)

        app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload() }.status.isOk()

        val puzzleHistoryItem = transaction {
            PuzzleHistoryItems.select { PuzzleHistoryItems.puzzleId eq 0 }.map { PuzzleHistoryDto.from(it) }
        }.firstOrNull()
        assertNotNull(puzzleHistoryItem)
    }

    @Test
    fun `submitPuzzleEndpoint returns OK and created PuzzleHistoryItems row with expected data`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(100)

        val moves = "d2d4 e7e5"
        val success = false

        val puzzleHistoryItemBefore = transaction {
            PuzzleHistoryItems.select { PuzzleHistoryItems.puzzleId eq 0 }.map { PuzzleHistoryDto.from(it) }
        }.firstOrNull()
        assertNull(puzzleHistoryItemBefore)

        app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload(success, moves) }.status.isOk()

        val puzzleHistoryItem = transaction {
            PuzzleHistoryItems.select { PuzzleHistoryItems.puzzleId eq 0 }.map { PuzzleHistoryDto.from(it) }
        }.firstOrNull()
        assertEquals(moves, puzzleHistoryItem?.moves)
        assertEquals(success, puzzleHistoryItem?.success)
    }

    @Test
    fun `submitPuzzleEndpoint returns OK and body value applied=false if this puzzle already submitted`() =
        testSuspend {
            setupUser()
            setupRandomPuzzlesWithId(100)

            transaction {
                PuzzleHistoryItems.insert {
                    it[moves] = ""
                    it[puzzleId] = 0
                    it[userId] = 0
                    it[success] = false
                }
            }

            val r = app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload(true, "e2e4") }
            r.jsonPath("$.applied", false)
        }

    @Test
    fun `submitPuzzleEndpoint returns OK and expected body if this puzzle already submitted`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(100)

        transaction {
            PuzzleHistoryItems.insert {
                it[moves] = ""
                it[puzzleId] = 0
                it[userId] = 0
                it[success] = false
            }
        }

        val r = app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload(true, "e2e4") }
        r.jsonPath("$.applied", false)
        r.jsonPath("$.rankingDiff", null)
        r.jsonPath("$.ranking", null)
    }

    @Test
    fun `submitPuzzleEndpoint returns OK and applied=true if this puzzle is not submitted before`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(100)

        val r = app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload(true, "e2e4") }
        r.jsonPath("$.applied", true)
    }

    @Test
    fun `submitPuzzleEndpoint returns OK and ranking, rankingDiff not null if this puzzle is not submitted before`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(100)

        val r = app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload(true, "e2e4") }
        r.jsonPath("$.applied", true)
        assertNotNull ( r.jsonPath<Int?>("$.ranking") )
        assertNotNull ( r.jsonPath<Int?>("$.rankingDiff") )
    }

    private fun getUserRanking(userId: Int): Int? {
        return transaction {
            TrainingRankings.select { TrainingRankings.userId eq userId }.map { it[TrainingRankings.ranking] }.singleOrNull()
        }
    }

    @Test
    fun `submitPuzzleEndpoint returns OK updates ranking in database`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(100)

        val old = getUserRanking(0)

        val r = app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload(true, "e2e4") }
        r.status.isOk()

        assertNotEquals(getUserRanking(0), old)
    }

    private fun setupUserRanking(userId: Int = 0, ranking: Int = 1500) = transaction {
        TrainingRankings.insert {
            it[TrainingRankings.userId] = userId
            it[TrainingRankings.ranking] = ranking
        }
    }

    @Test
    fun `submitPuzzleEndpoint returns OK and makes ranking greater if success=true`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(100)
        setupUserRanking()

        val old = getUserRanking(0) ?: 10000

        val r = app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload(true, "e2e4") }
        r.status.isOk()

        assertTrue((getUserRanking(0) ?: -10000) > old)
    }

    @Test
    fun `submitPuzzleEndpoint returns OK and makes ranking less than old if success=false`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(100)
        setupUserRanking()

        val old = getUserRanking(0) ?: -10000

        val r = app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload(false, "e2e4") }
        r.status.isOk()

        assertTrue((getUserRanking(0) ?: 10000) < old)
    }

    @Test
    fun `submitPuzzleEndpoint returns OK and expected body matching to ranking saved database`() = testSuspend {
        setupUser()
        setupRandomPuzzlesWithId(100)
        setupUserRanking()

        val oldRank = getUserRanking(0) ?: 0

        val r = app.client.post("/api/play/training/0/submit") { withToken(0); submitPayload(false, "e2e4") }
        r.status.isOk()

        val rank = getUserRanking(0) ?: 0

        r.jsonPath("$.ranking", rank)
        r.jsonPath("$.rankingDiff", rank - oldRank)
    }

    private fun setupUserWithRanking(
        rank: Int = 2000,
        userPrefs: UserStatisticsVisibility = UserStatisticsVisibility.EVERYONE
    ) = transaction {
        Users.insert {
            it[id] = 100
            it[username] = "rankeduser"
            it[emailAddress] = "ranked@gmail.com"
            it[passwordHash] = "test"
        }

        TrainingRankings.insert {
            it[userId] = 100
            it[ranking] = rank
        }

        UserPreferences.insert {
            it[userId] = 100
            it[id] = 50
            it[statisticsVisibility] = userPrefs
            it[historyVisibility] = UserPuzzleHistoryVisibility.ME
        }
    }

    @Test
    fun `getUserRankingEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.get("/api/play/training/ranking/100").status.isForbid()
    }

    @Test
    fun `getUserRankingEndpoint returns BAD_REQUEST if authenticated but stats are not visible for authenticated user - scenario 1`() =
        testSuspend {
            setupUser()
            setupUserWithRanking(userPrefs = UserStatisticsVisibility.ME)
            app.client.get("/api/play/training/ranking/100") { withToken(0) }.status.isBadRequest()
        }

    @Test
    fun `getUserRankingEndpoint returns BAD_REQUEST if authenticated but stats are not visible for authenticated user - scenario 2`() =
        testSuspend {
            setupUser()
            setupUserWithRanking(userPrefs = UserStatisticsVisibility.ONLY_FRIENDS)
            app.client.get("/api/play/training/ranking/100") { withToken(0) }.status.isBadRequest()
        }

    @Test
    fun `getUserRankingEndpoint returns OK if authenticated and user allows to see his stats for EVERYONE`() =
        testSuspend {
            setupUser()
            setupUserWithRanking()
            app.client.get("/api/play/training/ranking/100") { withToken(0) }.status.isOk()
        }

    @Test
    fun `getUserRankingEndpoint returns OK and expected body`() = testSuspend {
        val i = kotlin.random.Random.nextInt(0, 10000)
        setupUser()
        setupUserWithRanking(rank = i)
        val res = app.client.get("/api/play/training/ranking/100") { withToken(0) }
        res.jsonPath("$.ranking", i)
        res.jsonPath("$.userId", 100)
    }

    @Test
    fun `searchPuzzlesEndpoint returns FORBIDDEN if no authentication`() = testSuspend {
        app.client.post("/api/play/training/puzzles/search").status.isForbid()
    }

    private fun HttpRequestBuilder.searchPuzzlesBody(themeIds: List<Int> = listOf(0), from: Int = 500, to: Int = 1000) {
        jsonBody(
            "ranking" to mapOf(
                "from" to from,
                "to" to to
            ),

            "themeIds" to themeIds
        )
    }

    @Test
    fun `searchPuzzlesEndpoint returns OK if authenticated and BODY given`() = testSuspend {
        setupUser()
        app.client.post("/api/play/training/puzzles/search") { withToken(0); searchPuzzlesBody() }.status.isOk()
    }

    private fun setupDefaultThemes() {
        transaction {
            Themes.insert {
                it[id] = 0
                it[name] = "0"
            }

            Themes.insert {
                it[id] = 1
                it[name] = "1"
            }

            Themes.insert {
                it[id] = 2
                it[name] = "2"
            }
        }
    }

    @Test
    fun `searchPuzzlesEndpoint returns OK and expected items - scenario 1 -- 0 if one theme, but not both`() =
        testSuspend {
            setupUser()
            setupDefaultThemes()

            transaction {
                Puzzles.insert {
                    it[id] = 0
                    it[fen] = ""
                    it[moves] = ""
                    it[ownerId] = 0
                    it[database] = PuzzleDatabase.LICHESS
                    it[ranking] = 1500
                    it[openingId] = null
                }

                PuzzleThemes.insert {
                    it[puzzleId] = 0
                    it[themeId] = 1
                }
            }

            app.client.post("/api/play/training/puzzles/search") {
                withToken(0); searchPuzzlesBody(
                listOf(0, 1),
                0,
                2000
            )
            }.jsonPath("$.length()", 0)
        }

    @Test
    fun `searchPuzzlesEndpoint returns OK and expected items - scenario 1 -- 1 if both theme present`() = testSuspend {
        setupUser()
        setupDefaultThemes()

        transaction {
            Puzzles.insert {
                it[id] = 0
                it[fen] = ""
                it[moves] = ""
                it[ownerId] = 0
                it[database] = PuzzleDatabase.LICHESS
                it[ranking] = 1500
                it[openingId] = null
            }

            PuzzleThemes.insert {
                it[puzzleId] = 0
                it[themeId] = 0
            }

            PuzzleThemes.insert {
                it[puzzleId] = 0
                it[themeId] = 1
            }
        }

        app.client.post("/api/play/training/puzzles/search") { withToken(0); searchPuzzlesBody(listOf(0, 1), 0, 2000) }
            .jsonPath("$.length()", 1)
    }

    @Test
    fun `searchPuzzlesEndpoint returns OK and expected items - scenario 3 -- 0 if both theme present, but ranking range is bad`() =
        testSuspend {
            setupUser()
            setupDefaultThemes()

            transaction {
                Puzzles.insert {
                    it[id] = 0
                    it[fen] = ""
                    it[moves] = ""
                    it[ownerId] = 0
                    it[database] = PuzzleDatabase.LICHESS
                    it[ranking] = 1500
                    it[openingId] = null
                }

                PuzzleThemes.insert {
                    it[puzzleId] = 0
                    it[themeId] = 0
                }

                PuzzleThemes.insert {
                    it[puzzleId] = 0
                    it[themeId] = 1
                }
            }

            app.client.post("/api/play/training/puzzles/search") {
                withToken(0); searchPuzzlesBody(
                listOf(0, 1),
                1501,
                2000
            )
            }.jsonPath("$.length()", 0)
        }

    @Test
    fun `searchPuzzlesEndpoint returns OK and expected items - scenario 4 -- 2 if expected theme present`() =
        testSuspend {
            setupUser()
            setupDefaultThemes()

            transaction {
                Puzzles.insert {
                    it[id] = 0
                    it[fen] = ""
                    it[moves] = ""
                    it[ownerId] = 0
                    it[database] = PuzzleDatabase.LICHESS
                    it[ranking] = 1500
                    it[openingId] = null
                }

                Puzzles.insert {
                    it[id] = 1
                    it[fen] = ""
                    it[moves] = ""
                    it[ownerId] = 0
                    it[database] = PuzzleDatabase.LICHESS
                    it[ranking] = 1500
                    it[openingId] = null
                }

                PuzzleThemes.insert {
                    it[puzzleId] = 0
                    it[themeId] = 0
                }

                PuzzleThemes.insert {
                    it[puzzleId] = 1
                    it[themeId] = 0
                }
            }

            app.client.post("/api/play/training/puzzles/search") {
                withToken(0); searchPuzzlesBody(
                listOf(0),
                1499,
                2000
            )
            }.jsonPath("$.length()", 2)
        }

    @Test
    fun `searchPuzzlesEndpoint returns OK and expected items - scenario 5 -- 2 if no theme specified`() = testSuspend {
        setupUser()
        setupDefaultThemes()

        transaction {
            Puzzles.insert {
                it[id] = 0
                it[fen] = ""
                it[moves] = ""
                it[ownerId] = 0
                it[database] = PuzzleDatabase.LICHESS
                it[ranking] = 1500
                it[openingId] = null
            }

            Puzzles.insert {
                it[id] = 1
                it[fen] = ""
                it[moves] = ""
                it[ownerId] = 0
                it[database] = PuzzleDatabase.LICHESS
                it[ranking] = 1500
                it[openingId] = null
            }

            PuzzleThemes.insert {
                it[puzzleId] = 0
                it[themeId] = 0
            }
        }

        app.client.post("/api/play/training/puzzles/search") { withToken(0); searchPuzzlesBody(listOf(), 1499, 2000) }
            .jsonPath("$.length()", 2)
    }
}