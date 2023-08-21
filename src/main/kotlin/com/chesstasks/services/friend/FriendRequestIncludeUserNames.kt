package com.chesstasks.services.friend

class FriendRequestIncludeUserNames(
    val id: Int,
    val senderId: Int,
    val senderUserName: String?,
    val targetId: Int,
    val targetUserName: String?,
    val createdAt: Long
)