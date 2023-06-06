package com.chesstasks.requestvalidation

import com.chesstasks.controllers.puzzle.lichess.InsertLichessPuzzlePayload
import com.chesstasks.data.dto.LichessPuzzles
import com.chesstasks.services.chess.ChessService
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.java.KoinJavaComponent.inject

private fun isLichessPuzzleById(id: String): Boolean {
    return transaction {
        return@transaction LichessPuzzles.select { LichessPuzzles.id eq id }.count() > 0
    }
}

fun Application.configureRequestValidation() {
    val chessService: ChessService by inject(ChessService::class.java);

    install(RequestValidation) {
        // TODO: I want extract data (fen and moves) to interface and use one validator in user's and lichess db
        validate<InsertLichessPuzzlePayload> { payload ->
            val moveList = payload.moves.split(" ")
            if (moveList.isEmpty()) return@validate ValidationResult.Invalid("At least one move")
            if (payload.ranking !in 300..5000) return@validate ValidationResult.Invalid("Ranking in 300..5000")
            val moves = moveList.toTypedArray()
            val isPoseValid = chessService.validatePose(payload.fen, *moves)
            if (isLichessPuzzleById(payload.id)) {
                return@validate ValidationResult.Invalid("ID already taken")
            }
            if (isPoseValid) ValidationResult.Valid else ValidationResult.Invalid("FEN or SAN moves are invalid.")
        }
    }
}