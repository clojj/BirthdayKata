package com.clojj.birthdaykata.domain

import arrow.core.Either
import java.time.LocalDate

interface BirthdayService {

    fun birthdayMessages(employees: List<Employee>, date: LocalDate): Either<Throwable, List<EmailMessage>>

}
