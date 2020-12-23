package com.clojj.formvalidation.domain.service

import arrow.core.*
import arrow.core.computations.either
import com.clojj.formvalidation.domain.Input
import com.clojj.formvalidation.domain.InputEndoId
import com.clojj.formvalidation.domain.ValidationResultWithInput
import com.clojj.formvalidation.domain.ports.SomeRepository

interface Env : SomeRepository

suspend fun Env.formValidation(): Either<Throwable, Input> {

    val result = either<Throwable, Input> {
        val validatedForm: Input = validateForm(Input(42, "todo"))()
        validatedForm
    }

    result.fold({ println(it) }) { println("result $result") }

    return result
}

suspend fun validateForm(input: Input): ValidationResultWithInput<Input> {
    return Validated.fromNullable(null) { Tuple2(Endo(InputEndoId), "error").nel() }
}
