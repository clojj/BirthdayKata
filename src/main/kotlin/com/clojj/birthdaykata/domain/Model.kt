package com.clojj.birthdaykata.domain

import arrow.core.*
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.extension
import arrow.typeclasses.Semigroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

typealias ValidationResultWithInput<A> = Validated<Pair<Endo<A>, Nel<String>>, A>

val semigroup_singleton_endo: EndoSemigroup<Any?> = object : EndoSemigroup<Any?> {}

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <A> semigroupEndo(): EndoSemigroup<A> = semigroup_singleton_endo as EndoSemigroup<A>

@extension
interface EndoSemigroup<A> : Semigroup<Endo<A>> {
    override fun Endo<A>.combine(b: Endo<A>): Endo<A> = Endo { a -> b.f(this.f(a)) }
}

// TODO semigroup Pair

val semigroup_singleton_pair: PairSemigroup<Any?, Any?> = object : PairSemigroup<Any?, Any?> {}

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <A, B> semigroupPair(): PairSemigroup<Semigroup<A>, Semigroup<B>> = semigroup_singleton_pair as PairSemigroup<Semigroup<A>, Semigroup<B>>

@extension
interface PairSemigroup<A, B> : Semigroup<Pair<Semigroup<A>, Semigroup<B>>> {
    override fun Pair<Semigroup<A>, Semigroup<B>>.combine(b: Pair<Semigroup<A>, Semigroup<B>>): Pair<Semigroup<A>, Semigroup<B>> = Pair(this.first + b.first, this.second + b.second)
}


// --- inline classes

inline class EmailAddr @PublishedApi internal constructor(private val value: String) {
    override fun toString(): String = value
    companion object {
        fun parse(string: String): EmailAddress {
            check(string.contains('@')) { "Invalid email address." }
            return EmailAddress(string)
        }
    }
}

// ---

typealias ValidationResult<A> = ValidatedNel<String, A>

data class EmailAddress(val email: String) {

    companion object {

        operator fun invoke(email: String?): ValidationResult<EmailAddress> =
            if (email != null && email.contains("@")) EmailAddress(email).valid()
            else "Email must contain '@' found: '$email'".invalidNel()
    }
}

data class Employee(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val emailAddress: EmailAddress
) {

    companion object {

        private val DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu/MM/dd")

        private fun validateMin3(value: String): ValidationResult<String> =
            if (value.length >= 3) value.valid()
            else "Name must have at least 3 characters, found ${value.length}".invalidNel()

        private fun validateCase(value: String): ValidationResult<String> =
            if (value.first().isUpperCase()) value.valid()
            else "Name must start with uppercase, found '${value.first()}'".invalidNel()

        private fun validateNotNullOrBlank(value: String?): ValidationResult<String> {
            return if (!value.isNullOrBlank()) value.valid()
            else "Name must not be blank, found '$value'".invalidNel()
        }

        private fun validateName(name: String?): ValidationResult<String> =
            validateN(validateNotNullOrBlank(name), validateCase(name!!), validateMin3(name)).map { name }

        private fun validateDateOfBirth(dob: String?): ValidationResult<LocalDate> =
            try {
                if (dob == null) "Date of birth can't be empty. Found '$dob'".invalidNel()
                else LocalDate.parse(dob, DATE_FORMAT).valid()
            } catch (e: DateTimeParseException) {
                "Invalid date found: '$dob'".invalidNel()
            }

        operator fun invoke(
            firstName: String?,
            lastName: String?,
            dateOfBirth: String?,
            email: String?
        ): ValidationResult<Employee> =
            validateN(
                    validateName(firstName),
                    validateName(lastName),
                    validateDateOfBirth(dateOfBirth),
                    EmailAddress(email)
                ).map { (fn, ln, dob, e) -> Employee(fn, ln, dob, e) }
    }
}

data class Name(val name: String)

fun <A, B, C, D> validateN(
    vrA: ValidationResult<A>,
    vrB: ValidationResult<B>,
    vrC: ValidationResult<C>,
    vrD: ValidationResult<D>
) = ValidationResult.tupledN(NonEmptyList.semigroup(), vrA, vrB, vrC, vrD)

fun <A, B, C> validateN(
    vrA: ValidationResult<A>,
    vrB: ValidationResult<B>,
    vrC: ValidationResult<C>
) = ValidationResult.tupledN(NonEmptyList.semigroup(), vrA, vrB, vrC)


data class EmailMessage(
    val from: EmailAddress,
    val to: EmailAddress,
    val subject: String,
    val message: String
)
