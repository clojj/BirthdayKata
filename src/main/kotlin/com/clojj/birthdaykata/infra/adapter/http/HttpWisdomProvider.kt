package com.clojj.birthdaykata.infra.adapter.http

import com.clojj.birthdaykata.domain.ports.WisdomProvider

class HttpWisdomProvider: WisdomProvider {
    override suspend fun wisdoms(): List<String> {
/*
        // TODO Ktor server
        return HttpClient(CIO).use { client ->
            client.get<List<String>>("http://localhost:8080")
        }
*/
        return listOf("best things in life...", "stay calm")
    }
}
