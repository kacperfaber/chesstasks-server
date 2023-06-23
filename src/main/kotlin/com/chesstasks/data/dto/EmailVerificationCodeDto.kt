package com.chesstasks.data.dto

import com.chesstasks.data.BaseDto
import com.chesstasks.data.BaseTable
import com.fasterxml.jackson.annotation.JsonIgnore
import org.jetbrains.exposed.sql.ResultRow

object EmailVerificationCodes : BaseTable("email_verification_codes") {
    val code = varchar("code", 6).uniqueIndex()

    val passwordHash = varchar("password_hash", 32)
    val username = varchar("username", 32).uniqueIndex()
    val emailAddress = varchar("email_address", 32).uniqueIndex()
}

class EmailVerificationCodeDto(
    id: Int,
    val code: String,
    val username: String,
    val emailAddress: String,
    @JsonIgnore val passwordHash: String,
    createdAt: Long
) : BaseDto(id, createdAt) {
    companion object {
        fun from(r: ResultRow): EmailVerificationCodeDto = EmailVerificationCodeDto(
            r[EmailVerificationCodes.id],
            r[EmailVerificationCodes.code],
            r[EmailVerificationCodes.username],
            r[EmailVerificationCodes.emailAddress],
            r[EmailVerificationCodes.passwordHash],
            r[EmailVerificationCodes.createdAt],
        )
    }
}