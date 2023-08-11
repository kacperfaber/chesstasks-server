package com.chesstasks.controllers.api.user.preferences

import com.chesstasks.controllers.ofBoolean
import com.chesstasks.controllers.ofNullable
import com.chesstasks.controllers.requirePrincipalId
import com.chesstasks.data.dao.UserPuzzleHistoryVisibility
import com.chesstasks.data.dao.UserStatisticsVisibility
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.user
import com.chesstasks.services.user.preferences.UserPreferencesService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject

fun Route.userPreferencesController() {
    val userPreferencesService by inject<UserPreferencesService>(UserPreferencesService::class.java)

    user {
        get("user/preferences/history-visibility") {
            val value = userPreferencesService.getHistoryVisibility(call.requirePrincipalId())
            call.ofNullable(value)
        }

        post("user/preferences/history-visibility/{value}") {
            val value = UserPuzzleHistoryVisibility.valueOf(call.parameters["value"]?.uppercase() ?: throw MissingQueryParameter("value"))
            call.ofBoolean(userPreferencesService.setHistoryVisibility(call.requirePrincipalId(), value))
        }

        get("user/preferences/statistics-visibility") {
            call.ofNullable(userPreferencesService.getStatisticsVisibility(call.requirePrincipalId()))
        }

        post("user/preferences/statistics-visibility/{value}") {
            val valueRaw = call.parameters["value"] ?: throw MissingQueryParameter("value")
            val value = UserStatisticsVisibility.valueOf(valueRaw)
            call.ofBoolean(userPreferencesService.setStatisticsVisibility(call.requirePrincipalId(), value))
        }
    }
}