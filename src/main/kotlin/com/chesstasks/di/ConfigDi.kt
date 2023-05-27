package com.chesstasks.di

import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDi() {
    install(Koin) {
        val module = module {

        }

        modules()
    }
}