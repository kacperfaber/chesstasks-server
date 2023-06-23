package com.chesstasks

enum class Profile(val value: String) {
    PROD("prod"),
    DEV("dev"),
    TEST("test")
}

object Profiles {
    var profileFallback: Profile? = null

    val profile: Profile
        get() = profileFallback ?: getProfileFromEnv()

    fun isDev(): Boolean {
        return profile == Profile.DEV
    }

    fun isTest(): Boolean {
        return profile == Profile.TEST
    }

    fun isProd(): Boolean {
        return profile == Profile.PROD
    }

    private fun getProfileFromEnv(): Profile {
        return when (System.getProperty("profile")) {
            "test" -> Profile.TEST
            "dev" -> Profile.DEV
            "prod" -> Profile.PROD
            else -> throw Error("Unrecognized profile")
        }
    }
}