package com.chesstasks.game.training

import com.chesstasks.data.dto.PuzzleDto
import com.chesstasks.data.dto.UserDto
import com.chesstasks.game.GameSession
import com.chesstasks.game.GameSessions
import com.chesstasks.game.modes.TrainingGameSession
import com.chesstasks.game.modes.startTrainingSession
import com.chesstasks.game.puzzle.MoveSolveState
import com.chesstasks.game.puzzle.PuzzleController
import com.chesstasks.services.puzzle.PuzzleService
import com.chesstasks.services.training.ranking.TrainingRankingService
import com.chesstasks.websocket.exceptions.GameSessionNotFoundException
import io.ktor.server.websocket.*
import org.koin.core.annotation.Single

@Single
class TrainingController(private val trainingRankingService: TrainingRankingService, private val puzzleService: PuzzleService, private val puzzleStateController: PuzzleController) {
    class CreateSessionSettings(val rankingOffset: Int) {
        fun toTrainingSessionSettings(): TrainingGameSession.Settings {
            return TrainingGameSession.Settings(rankingOffset)
        }
    }

    fun createSession(user: UserDto, session: DefaultWebSocketServerSession, settings: CreateSessionSettings): GameSession {
        return GameSessions.startTrainingSession(user, settings.toTrainingSessionSettings(), session)
    }

    fun makeMove(session: DefaultWebSocketServerSession, move: String): MoveSolveState {
        val gameSession = GameSessions.get<TrainingGameSession>(session) ?: throw GameSessionNotFoundException()
        val puzzleState = gameSession.current ?: throw Exception("No current puzzle")
        return puzzleStateController.makeMove(puzzleState, move)
    }

    suspend fun nextPuzzle(session: DefaultWebSocketServerSession): PuzzleDto {
        val gameSession = GameSessions.get<TrainingGameSession>(session) ?: throw GameSessionNotFoundException()
        val ranking = trainingRankingService.getByUserId(gameSession.user.id).ranking
        val puzzleDto = puzzleService.getRandomByRankingRange(ranking - 300, ranking + 300).get(0)
        val puzzleState = puzzleStateController.createPuzzleState(puzzleId = puzzleDto.id, puzzleDto.fen, puzzleDto.moves.split(" "))
        gameSession.current = puzzleState
        return puzzleDto
    }
}