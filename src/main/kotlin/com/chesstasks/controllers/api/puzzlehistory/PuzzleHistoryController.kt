package com.chesstasks.controllers.api.puzzlehistory

import com.chesstasks.controllers.getSkip
import com.chesstasks.controllers.ofNullable
import com.chesstasks.controllers.requirePrincipalId
import com.chesstasks.security.auth.user
import com.chesstasks.services.puzzle.history.PuzzleHistoryService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject
import org.koin.ktor.ext.get

fun Route.puzzleHistoryController() {
    val puzzleHistoryService by inject<PuzzleHistoryService>(PuzzleHistoryService::class.java)

    // TODO: Add this to documentation.yaml

    user {
        get("/puzzle-history/mine") {
            val puzzleHistory = puzzleHistoryService.getAll(call.requirePrincipalId(), skip = call.getSkip())
            call.ofNullable(puzzleHistory)
        }
    }
}