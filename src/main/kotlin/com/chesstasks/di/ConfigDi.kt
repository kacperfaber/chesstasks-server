package com.chesstasks.di

import com.chesstasks.Profiles
import com.chesstasks.security.DevPasswordHasher
import com.chesstasks.security.ProdPasswordHasher
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
}

fun prodModule() = module {
    single {ProdPasswordHasher()}
}

fun Application.configureDi() {
    install(Koin) {
        val profileModule = if (Profiles.isDev()) devModule() else prodModule()
        modules(GlobalModule().module, profileModule)
    }
}