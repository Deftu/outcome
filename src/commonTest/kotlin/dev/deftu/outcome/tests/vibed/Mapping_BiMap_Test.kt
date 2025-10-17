package dev.deftu.outcome.tests.vibed

import dev.deftu.outcome.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Mapping_BiMap_Test {
    data class Raw(val n: String)
    data class Clean(val n: Int)

    private fun clean(raw: Raw): Outcome<Clean, String> {
        return Outcome.attempt({ "bad-int:${raw.n}" }) { raw.n.trim().toInt() }
            .mapValue { Clean(it) }
    }

    @Test
    fun mapping_BiMap() {
        val ok = clean(Raw(" 123 "))
            .bimap(
                transformValue = { c -> "ok:${c.n}" },
                transformError = { e -> "err:$e" }
            )
        println("ok=$ok")
        assertEquals("ok:123", ok.getOrThrow { IllegalStateException(it) })

        val bad = clean(Raw("nope"))
            .bimap(
                transformValue = { c -> "ok:${c.n}" },
                transformError = { e -> "err:$e" }
            )
        println("bad=$bad")
        assertTrue(bad.isFailure)
        assertEquals("err:bad-int:nope", bad.errorOrNull())
    }
}
