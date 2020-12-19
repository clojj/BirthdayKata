package com.clojj.birthdaykata

import com.clojj.birthdaykata.domain.BirthdayService
import com.clojj.birthdaykata.domain.BirthdayServiceInterpreter
import com.clojj.birthdaykata.domain.ports.EmailService
import com.clojj.birthdaykata.domain.ports.EmployeeRepository
import com.clojj.birthdaykata.domain.ports.WisdomProvider
import com.clojj.birthdaykata.domain.service.Env
import com.clojj.birthdaykata.domain.service.sendGreetingsUseCase
import com.clojj.birthdaykata.infra.adapter.http.HttpWisdomProvider
import com.clojj.birthdaykata.infra.adapter.repository.plain.FileEmployeeRepository
import com.clojj.birthdaykata.infra.mocks.SmtpEmailServiceMock
import java.time.LocalDate

// TODO demo with Ktor-, Hexagon-, Spring-, ... infra

// TODO UseCase for concurrent processing
//  unit-of-work: get employee -> fetch wisdoms (also concurrently itself!) -> create message -> send

suspend fun main() {

    val env: Env = object : Env,
        EmployeeRepository by FileEmployeeRepository("input.txt"),
        WisdomProvider by HttpWisdomProvider(),
        BirthdayService by BirthdayServiceInterpreter(),
        EmailService by SmtpEmailServiceMock() {}

    val results = env.sendGreetingsUseCase(date = LocalDate.parse("2020-11-21"))
    println("results: $results")
}
