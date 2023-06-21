package com.chesstasks.controllers.ui

import com.chesstasks.controllers.view
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get
import java.io.File

fun Route.configUI() {
    staticFiles("/public", File("src/main/resources/public")) {
        default("/css/style.css")
    }

    // TODO: Add controllers for both (admin and user)

    // TODO: Add admin controllers here.

    // TODO: Add user controllers here.

    get("/ui") {
        call.view("home/home")
    }
}