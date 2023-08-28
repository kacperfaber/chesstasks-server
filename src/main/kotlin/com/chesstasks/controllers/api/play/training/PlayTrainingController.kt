package com.chesstasks.controllers.api.play.training

import com.chesstasks.controllers.getSkip
import com.chesstasks.controllers.ofNullable
import com.chesstasks.controllers.requirePrincipalId
import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.user
import com.chesstasks.services.play.PlayService
import com.chesstasks.services.puzzle.PuzzleService
import com.chesstasks.services.training.ranking.TrainingRankingService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject


// TODO: No request validator set.

class GetPuzzlePayload(
    private val rankingOffset: Int?,
    private val themeId: Int?,
    private val database: PuzzleDatabase?
) {
    fun toPuzzleSearchCriteria(ranking: Int): PuzzleService.SearchCriteria {
        return PuzzleService.SearchCriteria(ranking, rankingOffset, themeId, database)
    }
}

data class SubmitPuzzlePayload(val success: Boolean, val moves: String)

fun Route.playTrainingController() {
    val puzzleService by inject<PuzzleService>(PuzzleService::class.java)
    val trainingRankingService by inject<TrainingRankingService>(TrainingRankingService::class.java)
    val playService by inject<PlayService>(PlayService::class.java)

    user {
        post("/play/training/puzzles") {
            val userId = call.requirePrincipalId()
            val userRanking = trainingRankingService.getByUserId(userId).ranking
            val searchCriteria = call.receive<GetPuzzlePayload>().toPuzzleSearchCriteria(userRanking)
            val puzzles = puzzleService.getRandomListBySearchCriteria(searchCriteria, skip = call.getSkip())
            call.ofNullable(puzzles)
        }

        post("/play/training/{puzzleId}/submit") {
            val userId = call.requirePrincipalId()
            val (success, moves) = call.receive<SubmitPuzzlePayload>()
            val puzzleId = call.parameters["puzzleId"]?.toIntOrNull() ?: throw MissingQueryParameter("puzzleId")
            val submitResult = playService.submitPuzzle(puzzleId, userId, moves, success)
            call.ofNullable(submitResult)
        }

        get("/play/training/ranking/{userId}") {
            val currUserId = call.requirePrincipalId()
            val userRanking = trainingRankingService.getByUserId(
                currUserId,
                call.parameters["userId"]?.toIntOrNull() ?: throw MissingQueryParameter("userId")
            )
            call.ofNullable(userRanking)
        }

        post("/play/training/puzzles/search") {
            val searchCriteria = call.receive<GetPuzzleSearchPayload>().toSearchPuzzleCriteria()
            call.ofNullable(puzzleService.searchPuzzles(searchCriteria))
        }
    }
}