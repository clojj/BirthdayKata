package com.clojj.birthdaykata.domain.ports

interface WisdomProvider {

    suspend fun wisdoms(): List<String>
}
