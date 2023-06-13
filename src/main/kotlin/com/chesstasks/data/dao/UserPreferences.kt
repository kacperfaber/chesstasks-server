package com.chesstasks.data.dao

import com.chesstasks.data.BaseTable
import com.chesstasks.data.dto.Users

enum class UserPuzzleHistoryVisibility(val i: Int) {
    ME(0),
    EVERYONE(1)
}

object UserPreferences : BaseTable("user_prefs") {
    val userId = integer("user_id").references(Users.id)
    val historyVisibility = enumeration<UserPuzzleHistoryVisibility>("history_visibility")
}