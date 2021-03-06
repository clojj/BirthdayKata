package com.clojj.birthdaykata.domain

import arrow.core.Either
import arrow.core.right
import java.time.LocalDate
import java.time.Month

class BirthdayServiceInterpreter : BirthdayService {

    override fun birthdayMessages(employees: List<Employee>, date: LocalDate): Either<Throwable, List<EmailMessage>> {
        return employees.filter { employeeFilter(date, it.dateOfBirth) }
            .map {
                EmailMessage(
                    SENDER_EMAIL,
                    it.emailAddress,
                    "Happy Birthday!",
                    "Happy birthday, dear ${it.firstName}!"
                )
            }.right()
    }

    private fun employeeFilter(date: LocalDate, birthday: LocalDate): Boolean =
        if (!date.isLeapYear && date.isFeb28th) birthday.isSameDay(date) || birthday.isFeb29th
        else birthday.isSameDay(date)

    companion object {

        private val SENDER_EMAIL = EmailAddress("birthday@corp.com")
    }
}

private fun LocalDate.isSameDay(date: LocalDate): Boolean =
    this.month == date.month && this.dayOfMonth == date.dayOfMonth

private val LocalDate.isFeb28th: Boolean
    get() = this.month == Month.FEBRUARY && this.dayOfMonth == 28

private val LocalDate.isFeb29th: Boolean
    get() = this.month == Month.FEBRUARY && this.dayOfMonth == 29
