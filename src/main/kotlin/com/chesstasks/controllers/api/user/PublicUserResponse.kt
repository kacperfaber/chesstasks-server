package com.chesstasks.controllers.api.user

import com.chesstasks.data.dto.UserDto

data class PublicUserResponse(val id: Int, val username: String) {
    companion object {
        fun fromNullableUser(userDto: UserDto?): PublicUserResponse? =
            if (userDto != null) PublicUserResponse(userDto.id, userDto.username) else null
    }
}