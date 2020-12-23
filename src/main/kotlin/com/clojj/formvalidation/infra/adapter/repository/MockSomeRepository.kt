package com.clojj.formvalidation.infra.adapter.repository

import com.clojj.formvalidation.domain.ports.SomeRepository

class MockSomeRepository(val listOf: List<Int>) : SomeRepository {
}
