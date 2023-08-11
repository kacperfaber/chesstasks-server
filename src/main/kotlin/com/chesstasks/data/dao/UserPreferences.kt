package com.chesstasks.data.dao

import com.chesstasks.data.BaseTable
import com.chesstasks.data.dto.Users

enum class UserPuzzleHistoryVisibility(val i: Int) {
    ME(0),
    EVERYONE(1),
    ONLY_FRIENDS(2)
}

enum class UserStatisticsVisibility(val i: Int) {
    ME(0),
    EVERYONE(1),
    ONLY_FRIENDS(2)
}

object UserPreferences : BaseTable("user_prefs") {
    val userId = integer("user_id").references(Users.id)
    val historyVisibility = enumeration<UserPuzzleHistoryVisibility>("history_visibility")
    val statisticsVisibility = enumeration<UserStatisticsVisibility>("statistics_visibility")
}