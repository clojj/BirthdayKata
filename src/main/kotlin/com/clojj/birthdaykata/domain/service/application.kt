package com.clojj.birthdaykata.domain.service

import arrow.core.Either
import arrow.core.computations.either
import arrow.fx.coroutines.parTraverseEither
import com.clojj.birthdaykata.domain.BirthdayService
import com.clojj.birthdaykata.domain.EmailMessage
import com.clojj.birthdaykata.domain.ports.EmailService
import com.clojj.birthdaykata.domain.ports.EmployeeRepository
import com.clojj.birthdaykata.domain.ports.WisdomProvider
import java.time.LocalDate

interface Env : EmployeeRepository, WisdomProvider, BirthdayService, EmailService

suspend fun Env.sendGreetingsUseCase(date: LocalDate): Either<Throwable, Int> {

    val result: Either<Throwable, Int> = either {

        val allEmployees = allEmployees()()

        // TODO ktor server
        // TODO Either
        val wisdoms = wisdoms()
        println(wisdoms)

        val greetings: List<EmailMessage> = birthdayMessages(allEmployees, date)()
        val results: List<String> = greetings.parTraverseEither {
            val sent = sendGreeting(it)
            sent
        }()
        results.size
    }
    result.fold({ println(it.message) }) { println("sent $it emails successfully") }
    return result
}
