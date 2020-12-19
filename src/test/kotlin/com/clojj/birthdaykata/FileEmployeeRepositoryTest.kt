package com.clojj.birthdaykata

import arrow.core.Either
import com.clojj.birthdaykata.domain.EmailAddress
import com.clojj.birthdaykata.domain.Employee
import com.clojj.birthdaykata.domain.ports.EmployeeRepository
import com.clojj.birthdaykata.infra.adapter.repository.plain.FileEmployeeRepository
import com.clojj.birthdaykata.infra.adapter.repository.plain.FileEmployeeRepository.EmployeeRepositoryException
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Integration tests, touches the file system
 */
class FileEmployeeRepositoryTest : StringSpec({

    "all employees are read from a valid CSV file" {
        val sut: EmployeeRepository = FileEmployeeRepository("input.txt")

        val allEmployees: Either<Throwable, List<Employee>> = sut.allEmployees()

        val expectedEmails = listOf(
            "john.doe@foobar.com",
            "mary.ann@foobar.com",
            "annabel.fly@gmail.com",
            "joe.hero@gmail.com",
            "lotta.bee@gmail.com"
        ).map(::EmailAddress)
        allEmployees.isRight() shouldBe true
        allEmployees.map {
            it.size shouldBe 5
            it.map { it.emailAddress } shouldBe expectedEmails
        }
    }

    "EmployeeRepositoryException when reading an invalid CSV file" {
        val sut: EmployeeRepository = FileEmployeeRepository("invalid_input.txt")

        val result = sut.allEmployees()

        result.fold({ (it as EmployeeRepositoryException).errors.size shouldBe 4 }) { fail("unexpected result $it") }
    }
})
