package dev.deftu.outcome.tests.vibed

import dev.deftu.outcome.attempt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Extension_Attempt_Slugify_Test {
    private fun String.slugOrThrow(): String {
        val base = this.trim()
        if (base.count { it.isLetterOrDigit() } < 3) throw IllegalArgumentException("too-short")
        return base.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    }

    @Test
    fun extension_Attempt_Slugify() {
        val ok = "  John Smith  ".attempt { slugOrThrow() }
        val bad = "  !!  ".attempt { slugOrThrow() }

        println("ok=$ok bad=$bad")

        assertEquals("john-smith", ok.getOrThrow { it })
        assertTrue(bad.isFailure)
        assertEquals("too-short", bad.errorOrNull()?.message)
    }
}
