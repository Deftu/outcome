package dev.deftu.outcome.tests.vibed

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceBootstrap_HappyPath_Test {
    @Test
    fun serviceBootstrap_HappyPath() {
        val text = """
            host=api.example.local
            port=8080
        """.trimIndent()

        val result = DemoDomain
            .parseProperties(text)
            .then { DemoDomain.assembleServiceProps(it) }
            .then { DemoDomain.openSocketLike(it.host, it.port) }

        println("== HappyPath RESULT == $result")

        val connected = result.getOrThrow { IllegalStateException(it) }
        assertEquals("CONNECTED(api.example.local:8080)", connected)
    }
}
