package com.clojj.birthdaykata.domain.ports

import arrow.core.Either
import com.clojj.birthdaykata.domain.Employee

interface EmployeeRepository {

    suspend fun allEmployees(): Either<Throwable, List<Employee>>
}
