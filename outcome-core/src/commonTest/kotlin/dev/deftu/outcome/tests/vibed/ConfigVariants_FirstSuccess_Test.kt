package dev.deftu.outcome.tests.vibed

import dev.deftu.outcome.firstSuccessOf
import kotlin.test.Test
import kotlin.test.assertTrue

class ConfigVariants_FirstSuccess_Test {
    @Test
    fun configVariants_FirstSuccess() {
        val candidates = listOf(
            "host=prod.example",
            """
            host=prod.example
            port=eighty
            """.trimIndent(),
            """
            host=prod.example
            port=443
            """.trimIndent()
        )

        val outcomes = candidates.map { text ->
            println("\n-- candidate --\n$text")
            DemoDomain
                .parseProperties(text)
                .then { DemoDomain.assembleServiceProps(it) }
                .then { DemoDomain.openSocketLike(it.host, it.port) }
        }

        val chosen = firstSuccessOf(*outcomes.toTypedArray())
        println("== chosen == $chosen")

        val value = chosen.getOrThrow { IllegalStateException(it) }
        assertTrue(value.startsWith("CONNECTED("))
        assertTrue(value.contains(":443)"))
    }
}
