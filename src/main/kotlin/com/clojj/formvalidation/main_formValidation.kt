package com.clojj.birthdaykata

import com.clojj.formvalidation.domain.ports.SomeRepository
import com.clojj.formvalidation.domain.service.Env
import com.clojj.formvalidation.domain.service.formValidation
import com.clojj.formvalidation.infra.adapter.repository.MockSomeRepository

suspend fun main() {

    val env: Env = object : Env, SomeRepository by MockSomeRepository(listOf(1, 2, 3, 42)) {}

    val results = env.formValidation()

    println("results: $results")
}
