package com.clojj.birthdaykata.infra

import arrow.core.Either
import arrow.core.right
import com.clojj.birthdaykata.domain.EmailAddress
import com.clojj.birthdaykata.domain.EmailMessage
import com.clojj.birthdaykata.domain.ports.EmailService
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class SmtpEmailService(private val host: String, private val port: Int) : EmailService {

    override suspend fun sendGreeting(emailMessage: EmailMessage): Either<Throwable, String> {
        val session =  buildSession()
        val message = createMessage(session, emailMessage)
        return Either.catch {
            Transport.send(message)
            println("sent")
            message.subject
        }
        return emailMessage.to.toString().right()
    }

    private fun buildSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port.toString())
        }

        return Session.getInstance(props, null)
    }

    private fun createMessage(session: Session, emailMessage: EmailMessage): Message =
        MimeMessage(session).apply {
            setFrom(emailMessage.from.toInternetAddress())
            setRecipient(Message.RecipientType.TO, emailMessage.to.toInternetAddress())
            subject = emailMessage.subject
            setText(emailMessage.message)
        }

    private fun EmailAddress.toInternetAddress() = InternetAddress(email)
}
