package dev.deftu.outcome.tests.vibed

import dev.deftu.outcome.IllegalUnwrapException
import dev.deftu.outcome.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Result_Integrations_Test {
    private class IsolatedThrowable(message: String) : Throwable(message) {
        override fun toString(): String = "IsolatedThrowable: $message"
    }

    @Test
    fun result_Integrations() {
        val rOk = runCatching { "ping".uppercase() }
        val rErr = runCatching { throw IsolatedThrowable("down") }

        val oOk = Outcome.fromResult(rOk)
        val oErr = Outcome.fromResult(rErr)

        println("oOk=$oOk oErr=$oErr")

        assertEquals("PING", oOk.unwrap())

        val thrown1 = assertFailsWith<IllegalUnwrapException> {
            oErr.expect("service-failure")
        }
        assertEquals(true, thrown1.message?.contains("service-failure"))

        val thrown2 = assertFailsWith<Throwable> { oErr.unwrap() }
        assertEquals("Tried to unwrap a Failure(IsolatedThrowable: down)", thrown2.message)
    }
}
