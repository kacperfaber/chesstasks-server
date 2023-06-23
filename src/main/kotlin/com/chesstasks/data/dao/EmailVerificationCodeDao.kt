package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.EmailVerificationCodeDto
import com.chesstasks.data.dto.EmailVerificationCodes
import com.chesstasks.data.dto.Users
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single

@Single
class EmailVerificationCodeDao {
    suspend fun getByCodeAndEmailAddress(code: String, emailAddress: String): EmailVerificationCodeDto? = dbQuery {
        EmailVerificationCodes
            .join(Users, JoinType.LEFT, additionalConstraint = { Users.id eq EmailVerificationCodes.userId })
            .select { (EmailVerificationCodes.code eq code) and (Users.emailAddress like emailAddress) }
            .map(EmailVerificationCodeDto::from)
            .firstOrNull()
    }

    suspend fun deleteById(id: Int): Boolean = dbQuery{
        EmailVerificationCodes.deleteWhere { EmailVerificationCodes.id eq id } > 0
    }
}