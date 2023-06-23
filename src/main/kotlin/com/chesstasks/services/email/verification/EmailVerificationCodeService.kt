package com.chesstasks.services.email.verification

import com.chesstasks.data.dao.EmailVerificationCodeDao
import com.chesstasks.data.dto.EmailVerificationCodeDto
import org.koin.core.annotation.Single

@Single
class EmailVerificationCodeService(private val emailVerificationCodeDao: EmailVerificationCodeDao) {
    suspend fun getByEmailAndCode(email: String, code: String): EmailVerificationCodeDto? =
        emailVerificationCodeDao.getByCodeAndEmailAddress(code, email)

    suspend fun deleteById(id: Int): Boolean =
        emailVerificationCodeDao.deleteById(id)
}