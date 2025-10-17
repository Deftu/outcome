package dev.deftu.outcome.tests.vibed

import dev.deftu.outcome.Outcome
import dev.deftu.outcome.flatten
import dev.deftu.outcome.toOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Conversions_And_Mapping_Test {
    private fun mightThrow(flag: String): String {
        if (flag == "boom") throw IllegalStateException("KABOOM")
        return "OK:$flag"
    }

    @Test
    fun conversions_And_Mapping() {
        val okRes = runCatching { mightThrow("fine") }.toOutcome()
        val errRes = runCatching { mightThrow("boom") }.toOutcome()

        println("okRes=$okRes errRes=$errRes")
        assertEquals("OK:fine", okRes.getOrThrow { it })
        assertTrue(errRes.isFailure)
        assertEquals("KABOOM", errRes.errorOrNull()?.message)

        val fromPairOk = ("value" to null).toOutcome<String, String>()
        val fromPairErr = (null to "bad").toOutcome<String, String>()

        println("fromPairOk=$fromPairOk fromPairErr=$fromPairErr")
        assertEquals("value", fromPairOk.getOrThrow { IllegalStateException(it) })
        assertEquals("bad", fromPairErr.errorOrNull())

        val mappedErr = errRes.mapError { e -> "mapped:${e.message}" }
        println("mappedErr=$mappedErr")
        assertEquals("mapped:KABOOM", mappedErr.errorOrNull())

        val nested: Outcome<Outcome<Int, String>, String> = Outcome.success(Outcome.success(42))
        val flat = nested.flatten()
        println("flatten=$flat")
        assertEquals(42, flat.getOrThrow { IllegalStateException(it) })
    }
}
