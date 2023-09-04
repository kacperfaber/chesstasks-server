package com.chesstasks.services.email.verification

import com.chesstasks.Properties
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail

// TODO: Rebuild it, move Dev to test or IDK.

interface VerificationEmailSender {
    suspend fun sendVerificationEmail(emailAddress: String, code: String)
}

class ProdVerificationEmailSender : VerificationEmailSender {
    private val hostName by Properties.value<String>("$.email.verification.hostname")
    private val port by Properties.value<Int>("$.email.verification.port")
    private val username by Properties.value<String>("$.email.verification.username")
    private val password by Properties.value<String>("$.email.verification.password")
    private val from by Properties.value<String>("$.email.verification.from")
    private val subject by Properties.value<String>("$.email.verification.subject")

    override suspend fun sendVerificationEmail(emailAddress: String, code: String) {
        val msg = """
            Hi!
            
            If you still want to register in <a href="ChessTasks.com">ChessTasks.com</a>, 
            please use this verification code: <strong>$code</strong>.
            
            If it wasn't you, please ignore this email.
        """.trimIndent()

        SimpleEmail().apply {
            hostName = this@ProdVerificationEmailSender.hostName
            setSmtpPort(port)
            setAuthenticator(DefaultAuthenticator(username, password))
            setFrom(from)
            subject = this@ProdVerificationEmailSender.subject
            setMsg(msg)
            addTo(emailAddress)
        }.send()
    }
}

class DevVerificationEmailSender : VerificationEmailSender {
    override suspend fun sendVerificationEmail(emailAddress: String, code: String) {
        println("\nDevVerificationEmailSender: Sending code $code to $emailAddress\n")
    }
}