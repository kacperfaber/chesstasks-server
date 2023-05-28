package com.chesstasks.di

import com.chesstasks.Profiles
import com.chesstasks.security.DevPasswordHasher
import com.chesstasks.security.PasswordHasher
import com.chesstasks.security.ProdPasswordHasher
import com.chesstasks.services.token.*
import com.google.gson.Gson
import io.ktor.server.application.*
import org.koin.core.KoinApplication
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.context.startKoin
import org.koin.dsl.binds
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin

@Module
@ComponentScan(value = "com.chesstasks")
class GlobalModule

fun devModule() = module {
    single { DevPasswordHasher() } binds arrayOf(PasswordHasher::class)
    single { DevTokenReader(get()) } binds arrayOf(TokenReader::class)
    single { DevTokenWriter(get()) } binds arrayOf(TokenWriter::class)
}

fun prodModule() = module {
    single { ProdPasswordHasher() } binds arrayOf(PasswordHasher::class)
    single { ProdTokenReader() } binds arrayOf(TokenReader::class)
    single { ProdTokenWriter() } binds arrayOf(TokenWriter::class)
}

fun gsonModule() = module {
    single { Gson() }
}

fun KoinApplication.setupModules() {
    val profileModule = if (Profiles.isProd()) prodModule() else devModule()
    modules(GlobalModule().module, profileModule, gsonModule())
}

fun Application.configureDi() {
    install(Koin) {}

    startKoin {
        setupModules()
    }
}