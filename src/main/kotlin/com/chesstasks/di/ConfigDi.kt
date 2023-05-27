package com.chesstasks.di

import io.ktor.server.application.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin

@Module
@ComponentScan(value = "com.chesstasks")
class AppModule

fun Application.configureDi() {
    install(Koin) {
        modules(AppModule().module)
    }
}