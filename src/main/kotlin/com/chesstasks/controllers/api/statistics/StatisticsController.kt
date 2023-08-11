package com.chesstasks.controllers.api.statistics

import com.chesstasks.controllers.ofNullable
import com.chesstasks.controllers.requirePrincipalId
import com.chesstasks.exceptions.MissingQueryParameter
import com.chesstasks.security.auth.user
import com.chesstasks.services.statistics.StatisticsService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject

fun Route.statisticsController() {
    val statisticsService by inject<StatisticsService>(StatisticsService::class.java)

    user {
        get("statistics/simple/user/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull() ?: throw MissingQueryParameter("userId")
            call.ofNullable(statisticsService.getSimpleStatisticsFor(userId, call.requirePrincipalId()))
        }
    }
}