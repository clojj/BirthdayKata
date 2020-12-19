package com.clojj.birthdaykata.infra.mocks

import arrow.core.Either
import arrow.core.Right
import com.clojj.birthdaykata.domain.EmailMessage
import com.clojj.birthdaykata.domain.ports.EmailService


class SmtpEmailServiceMock() : EmailService {

    override suspend fun sendGreeting(emailMessage: EmailMessage): Either<Throwable, String> {
        println("sending ${emailMessage.to.email}")
        return Right(emailMessage.to.email)
    }

}
