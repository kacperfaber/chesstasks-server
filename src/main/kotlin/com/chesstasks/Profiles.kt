package com.chesstasks

enum class Profile(val value: String) {
    PROD("prod"),
    DEV("dev"),
    TEST("test")
}

object Profiles {
    var profileFallback: Profile = Profile.DEV

    val profile: Profile
        get() {
            return try { getProf() } catch (e: Exception) { profileFallback }
        }

    fun isDev(): Boolean {
        return getProf() == Profile.DEV
    }

    fun isTest(): Boolean {
        return getProf() == Profile.TEST
    }

    fun isProd(): Boolean {
        return getProf() == Profile.PROD
    }

    private fun getProf(): Profile {
        return when (System.getProperty("com.chesstasks.profile").lowercase()) {
            "test" -> Profile.TEST
            "dev" -> Profile.DEV
            "prod" -> Profile.PROD
            else -> profileFallback
        }
    }
}