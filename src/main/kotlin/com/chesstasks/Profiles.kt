package com.chesstasks

object Profiles {
    private val profile: String = System.getProperty("profile")

    fun isDev(): Boolean {
        return profile == "dev"
    }

    fun isProd(): Boolean {
        return profile == "prod"
    }

    fun getProfile() = profile
}