package dev.deftu.outcome.tests.vibed

import dev.deftu.outcome.Outcome
import dev.deftu.outcome.ensureNotNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Pricing_DiscountFlow_Test {
    private fun parseCartTotal(raw: String?): Outcome<Int, String> {
        return Outcome.nullable(raw) { "missing total" }
            .then { text -> Outcome.attempt({ "bad number: $text" }) { text.trim().toInt() } }
            .filterOrElse(predicate = { it >= 0 }, onFailure = { "negative total: $it" })
    }

    private fun applyCoupon(total: Int, code: String?): Outcome<Int, String> {
        val normalized = Outcome.nullable(code) { "no coupon" }.mapValue { it.trim().lowercase() }

        return normalized.orElse { _ -> Outcome.success("none") }
            .fold(
                onSuccess = { c ->
                    when (c) {
                        "none" -> Outcome.success(total)
                        "save10" -> Outcome.success((total * 0.9).toInt())
                        "flat50" -> Outcome.success((total - 50).coerceAtLeast(0))
                        else -> Outcome.failure("unknown coupon: $c")
                    }
                },
                onFailure = { e -> Outcome.failure(e) }
            )
    }

    @Test
    fun pricing_DiscountFlow() {
        val total = parseCartTotal("250").ensureNotNull { "total vanished" }

        val withCoupon = total.then { applyCoupon(it, "save10") }
        println("withCoupon=$withCoupon")
        assertEquals(Outcome.success(225), withCoupon)

        val badCoupon = total.then { applyCoupon(it, "WOOHOO") }
        println("badCoupon=$badCoupon")
        assertTrue(badCoupon.isFailure)
        assertEquals("unknown coupon: woohoo", badCoupon.errorOrNull())

        val noCoupon = total.then { applyCoupon(it, null) }
        println("noCoupon=$noCoupon")
        assertEquals(Outcome.success(250), noCoupon)
    }
}
