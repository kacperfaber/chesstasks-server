package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import org.jetbrains.exposed.sql.ResultRow

object EmailVerificationCodes:BaseTable("email_verification_codes") {
    val code = varchar("code" , 6)
    val userId = integer("user_id").references(Users.id)
}

class EmailVerificationCodeDto(id: Int, val code: String, val user: UserDto, createdAt: Long) : BaseDto(id, createdAt) {
    companion object {
        fun from(r: ResultRow): EmailVerificationCodeDto = EmailVerificationCodeDto(
            r[EmailVerificationCodes.id],
            r[EmailVerificationCodes.code],
            UserDto.tryFrom(r)!!,
            r[EmailVerificationCodes.createdAt],
        )
    }
}