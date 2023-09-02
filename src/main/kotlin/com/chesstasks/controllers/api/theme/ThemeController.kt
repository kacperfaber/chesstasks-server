package com.chesstasks.controllers.api.theme

import com.chesstasks.controllers.ofNullable
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.admin
import com.chesstasks.security.auth.user
import com.chesstasks.services.puzzle.themes.PuzzleThemeService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject

fun Route.themeController() {
    val puzzleThemeService by inject<PuzzleThemeService>(PuzzleThemeService::class.java)

    user {
        get("/theme/all") {
            call.ofNullable(puzzleThemeService.getThemes())
        }
    }

    admin {
        put("/theme/as-admin/{name}") {
            val themeName = call.parameters["name"] ?: throw MissingQueryParameter("name")
            call.ofNullable(puzzleThemeService.tryInsertTheme(themeName))
        }
    }
}