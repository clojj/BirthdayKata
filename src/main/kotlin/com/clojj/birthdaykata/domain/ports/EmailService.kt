package com.clojj.birthdaykata.domain.ports

import arrow.core.Either
import com.clojj.birthdaykata.domain.EmailMessage

interface EmailService {

    suspend fun sendGreeting(emailMessage: EmailMessage): Either<Throwable, String>

}
