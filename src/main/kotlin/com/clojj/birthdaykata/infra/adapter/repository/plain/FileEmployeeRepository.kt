package com.clojj.birthdaykata.infra.adapter.repository.plain

import arrow.core.*
import arrow.core.extensions.list.traverse.sequence
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicative.applicative
import arrow.fx.coroutines.bracket
import com.clojj.birthdaykata.domain.Employee
import com.clojj.birthdaykata.domain.ValidationResult
import com.clojj.birthdaykata.domain.ports.EmployeeRepository
import java.io.BufferedReader
import java.io.File

class FileEmployeeRepository(fileName: String) : EmployeeRepository {

    private val file = File(fileName)

    override suspend fun allEmployees(): Either<Throwable, List<Employee>> =
        bracket({ file.bufferedReader() }, readFile(), { it.close() })

    private fun readFile(): suspend (BufferedReader) -> Either<Throwable, List<Employee>> = { br: BufferedReader ->
        val employees =
            br.readLines()
                .drop(1)
                .map(employeeParser)

        val validatedEmployees: Validated<NonEmptyList<String>, List<Employee>> = sequence(employees)

        // TODO replace exception here ?
        validatedEmployees.fold({ Either.left(EmployeeRepositoryException(it)) }, { Either.right(it) })
    }

    // TODO
    private fun sequence(input: List<ValidationResult<Employee>>): ValidationResult<List<Employee>> =
        input.sequence(ValidationResult.applicative(Nel.semigroup()))
            .fix()
            .map { it.fix() }

    data class EmployeeRepositoryException(
        val errors: Nel<String>
    ) : RuntimeException("Error reading from repo: $errors")

    companion object {

        val employeeParser: (String) -> ValidationResult<Employee> = { row ->
            val parts = row.split(", ")
            val lastName = parts.getOrNull(0)
            val firstName = parts.getOrNull(1)
            val dateOfBirth = parts.getOrNull(2)
            val email = parts.getOrNull(3)
            Employee(firstName, lastName, dateOfBirth, email)
        }
    }
}
