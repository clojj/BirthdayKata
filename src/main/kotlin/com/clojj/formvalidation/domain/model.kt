package com.clojj.formvalidation.domain

import arrow.core.*
import com.clojj.birthdaykata.domain.EmailAddress

typealias InputWithErrors<A> = Tuple2<Endo<A>, String>

typealias ValidationResultWithInput<A> = ValidatedNel<InputWithErrors<A>, A>

// TODO what package for this ? it is used from web adapter ?
data class Input(val int: Int, val string: String)

typealias InputEndo = (Input) -> Input

val InputEndoId: InputEndo = { it }




// --- TODO test new inline classes

inline class Email @PublishedApi internal constructor(private val value: String) {
    override fun toString(): String = value
    companion object {
        fun parse(string: String): EmailAddress {
            check(string.contains('@')) { "Invalid email address." }
            return EmailAddress(string)
        }
    }
}

