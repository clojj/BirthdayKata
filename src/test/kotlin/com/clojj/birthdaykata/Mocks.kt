package com.clojj.birthdaykata

import arrow.core.Either
import com.clojj.birthdaykata.domain.Employee
import com.clojj.birthdaykata.domain.ports.EmployeeRepository
import java.io.IOException

class FailingFileEmployeeRepository : EmployeeRepository {
    override suspend fun allEmployees(): Either<Throwable, List<Employee>> {
        return Either.catch {
            // some actual IO here...
            throw IOException("outage !")
        }
    }
}
