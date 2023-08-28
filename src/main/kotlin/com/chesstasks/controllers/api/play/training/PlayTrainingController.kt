package com.chesstasks.controllers.api.play.training

import com.chesstasks.controllers.getSkip
import com.chesstasks.controllers.ofNullable
import com.chesstasks.controllers.requirePrincipalId
import com.chesstasks.data.dto.PuzzleDatabase
import com.chesstasks.exceptions.BadRequestException
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.user
import com.chesstasks.services.puzzle.PuzzleService
import com.chesstasks.services.puzzle.history.PuzzleHistoryService
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

class SubmitPuzzleResponse(val rankingDifference: Int, val ranking: Int)

fun Route.playTrainingController() {
    val puzzleService by inject<PuzzleService>(PuzzleService::class.java)
    val trainingRankingService by inject<TrainingRankingService>(TrainingRankingService::class.java)
    val puzzleHistoryService by inject<PuzzleHistoryService>(PuzzleHistoryService::class.java)

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
            val userRanking = trainingRankingService.getByUserId(userId).ranking
            val puzzleId = call.parameters["puzzleId"]?.toIntOrNull() ?: throw MissingQueryParameter("puzzleId")
            val puzzle = puzzleService.getById(puzzleId) ?: throw BadRequestException()
            val (success,moves) = call.receive<SubmitPuzzlePayload>()
            val newRanking = trainingRankingService.updateRanking(userId, userRanking, puzzle.ranking, success)
            puzzleHistoryService.submitPuzzleHistory(userId, puzzleId, moves, success)
            call.ofNullable(SubmitPuzzleResponse(userRanking - newRanking, newRanking))
        }

        get("/play/training/ranking/{userId}") {
            val currUserId = call.requirePrincipalId()
            val userRanking = trainingRankingService.getByUserId(currUserId, call.parameters["userId"]?.toIntOrNull() ?: throw MissingQueryParameter("userId"))
            call.ofNullable(userRanking)
        }

        post("/play/training/puzzles/search") {
            val searchCriteria = call.receive<GetPuzzleSearchPayload>().toSearchPuzzleCriteria()
            call.ofNullable(puzzleService.searchPuzzles(searchCriteria))
        }
    }
}