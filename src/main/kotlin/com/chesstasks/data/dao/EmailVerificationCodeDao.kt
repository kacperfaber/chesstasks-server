package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.EmailVerificationCodeDto
import com.chesstasks.data.dto.EmailVerificationCodes
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single

@Single
class EmailVerificationCodeDao {
    suspend fun getByCodeAndEmailAddress(code: String, emailAddress: String): EmailVerificationCodeDto? = dbQuery {
        EmailVerificationCodes
            .select { (EmailVerificationCodes.code eq code) and (EmailVerificationCodes.emailAddress like emailAddress) }
            .map(EmailVerificationCodeDto::from)
            .firstOrNull()
    }

    suspend fun deleteById(id: Int): Boolean = dbQuery {
        EmailVerificationCodes.deleteWhere { EmailVerificationCodes.id eq id } > 0
    }

    suspend fun insertValues(emailAddress: String, username: String, passwordHash: String): EmailVerificationCodeDto? =
        dbQuery {
            EmailVerificationCodes.insert {
                it[EmailVerificationCodes.emailAddress] = emailAddress
                it[EmailVerificationCodes.username] = username
                it[EmailVerificationCodes.passwordHash] = passwordHash
            }
                .resultedValues
                ?.map(EmailVerificationCodeDto::from)
                ?.firstOrNull()
        }
}