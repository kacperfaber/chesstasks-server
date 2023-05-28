package com.chesstasks.di

import com.chesstasks.Profiles
import com.chesstasks.security.DevPasswordHasher
import com.chesstasks.security.ProdPasswordHasher
import com.chesstasks.services.token.DevTokenReader
import com.chesstasks.services.token.DevTokenWriter
import com.chesstasks.services.token.ProdTokenReader
import com.chesstasks.services.token.ProdTokenWriter
import com.google.gson.Gson
import io.ktor.server.application.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin

@Module
@ComponentScan(value = "com.chesstasks")
class GlobalModule

fun devModule() = module {
    single { DevPasswordHasher() }
    single { DevTokenReader(get()) }
    single { DevTokenWriter(get()) }
}

fun prodModule() = module {
    single { ProdPasswordHasher() }
    single { ProdTokenReader() }
    single { ProdTokenWriter() }
}

fun gsonModule() = module {
    single { Gson() }
}

fun Application.configureDi() {
    install(Koin) {
        val profileModule = if (Profiles.isDev()) devModule() else prodModule()
        modules(GlobalModule().module, profileModule, gsonModule())
    }
}