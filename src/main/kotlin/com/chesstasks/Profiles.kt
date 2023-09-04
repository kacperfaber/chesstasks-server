package com.chesstasks

enum class Profile(val value: String) {
    PROD(Constants.ProfileProd),
    DEV(Constants.ProfileDev),
    TEST(Constants.ProfileProd)
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
        return when (System.getProperty(Constants.ProfileVar).lowercase()) {
            Constants.ProfileTest -> Profile.TEST
            Constants.ProfileDev -> Profile.DEV
            Constants.ProfileProd -> Profile.PROD
            else -> profileFallback
        }
    }
}