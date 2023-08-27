package com.chesstasks.requestvalidation

import com.chesstasks.controllers.api.register.RegisterPayload
import com.chesstasks.requestvalidation.base.InsertPuzzlePayload
import com.chesstasks.services.chess.ChessService
import com.chesstasks.validateEmailAddress
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import org.koin.java.KoinJavaComponent.inject

fun Application.configureRequestValidation() {
    val chessService: ChessService by inject(ChessService::class.java);

    install(RequestValidation) {
        validate<InsertPuzzlePayload> {payload ->
            val moveList = payload.moves.split(" ").toTypedArray()
            if (moveList.isEmpty()) return@validate ValidationResult.Invalid("At least one move")
            val isPoseValid = chessService.validatePose(payload.fen, *moveList)
            if(isPoseValid)ValidationResult.Valid else ValidationResult.Invalid("FEN or SAN moves are invalid.")
        }

        validate<RegisterPayload> { p ->
            if (!p.emailAddress.validateEmailAddress()) {
                return@validate ValidationResult.Invalid("Bad email address")
            }

            if (p.username.length < 2) {
                return@validate ValidationResult.Invalid("Bad email address")
            }

            // TODO: add rules for passwords.

            ValidationResult.Valid
        }
    }
}