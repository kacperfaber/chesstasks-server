package com.chesstasks.controllers.api.statistics

import com.chesstasks.security.auth.user
import io.ktor.server.routing.*

fun Route.statisticController() {
    user {
        get("statistics/user/{userId}") {

        }
    }
}