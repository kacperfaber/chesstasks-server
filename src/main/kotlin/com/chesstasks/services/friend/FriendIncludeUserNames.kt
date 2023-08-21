package com.chesstasks.services.friend

data class FriendIncludeUserNames(val id: Int, val userId: Int, val secondUserId: Int, val createdAt: Long, val userName: String?, val secondUserName: String?)