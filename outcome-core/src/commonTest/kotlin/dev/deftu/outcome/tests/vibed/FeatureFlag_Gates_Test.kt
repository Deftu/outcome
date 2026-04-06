package dev.deftu.outcome.tests.vibed

import dev.deftu.outcome.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureFlag_Gates_Test {
    private fun isRegionAllowed(region: String): Outcome<Boolean, String> {
        val allowed = setOf("eu", "na", "za")
        return Outcome.bool(region.lowercase() in allowed) { "region not allowed: $region" }
            .onSuccess { println("[gate:region] pass=$region") }
            .onFailure { println("[gate:region] fail=$it") }
    }

    private fun isUserAgeOk(age: Int): Outcome<Boolean, String> {
        return Outcome.bool(age >= 16) { "too young: $age" }
            .onSuccess { println("[gate:age] pass=$age") }
            .onFailure { println("[gate:age] fail=$it") }
    }

    @Test
    fun featureFlag_Gates() {
        val both = isRegionAllowed("ZA").zip(isUserAgeOk(18))
        println("both=$both")
        assertTrue(both.isSuccess)

        val fail = isRegionAllowed("Antarctica").zip(isUserAgeOk(99))
        println("fail=$fail")
        assertTrue(fail.isFailure)
        assertEquals("region not allowed: Antarctica", fail.errorOrNull())
    }
}
