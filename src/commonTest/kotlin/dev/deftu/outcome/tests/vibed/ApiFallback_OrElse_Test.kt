package dev.deftu.outcome.tests.vibed

import dev.deftu.outcome.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiFallback_OrElse_Test {
    private fun callApi(base: String): Outcome<String, String> {
        return when (base) {
            "https://primary" -> Outcome.failure("HTTP 503")
            "https://mirror-a" -> Outcome.success("200 OK from mirror-a")
            else -> Outcome.failure("DNS")
        }.also { println("[callApi] $base -> $it") }
    }

    @Test
    fun apiFallback_OrElse() {
        val primary = callApi("https://primary")
        val resolved = primary.orElse { _ -> callApi("https://mirror-a") }
        println("resolved=$resolved")
        assertEquals("200 OK from mirror-a", resolved.getOrThrow { IllegalStateException(it) })
    }
}
