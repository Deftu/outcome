package dev.deftu.outcome.tests.vibed

import dev.deftu.outcome.Outcome
import dev.deftu.outcome.collectAll
import dev.deftu.outcome.firstSuccessOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
public class Microbench_Smoke_Test {

    private data class BenchResult(
        val name: String,
        val iters: Int,
        val elapsed: Duration,
        val opsPerSec: Double,
        val checksum: Long,
    )

    // 64-bit FNV-1a style mixing, KMP-safe (overflow is fine on all targets)
    private inline fun mix(checksum: Long, res: Long, i: Int): Long {
        val x = checksum xor (res + ((i.toLong() shl 1) or 1L))
        return x * 1099511628211L // FNV prime
    }

    private fun runBench(
        name: String,
        iterations: Int = 250_000,
        warmup: Int = iterations / 10,
        block: (i: Int) -> Long,
    ): BenchResult {
        // Warmup
        var warm = 1469598103934665603L // FNV offset
        for (i in 0 until warmup) {
            warm = mix(warm, block(i), i)
        }

        val mark = TimeSource.Monotonic.markNow()
        var checksum = 1469598103934665603L
        for (i in 0 until iterations) {
            checksum = mix(checksum, block(i), i)
        }
        val elapsed = mark.elapsedNow()

        val seconds = elapsed.inWholeNanoseconds / 1_000_000_000.0
        val opsPerSec = if (seconds > 0.0) iterations / seconds else Double.POSITIVE_INFINITY

        println(
            "bench[$name] elapsed=${elapsed.inWholeMilliseconds}ms " +
                    "iters=$iterations ops/sec=${opsPerSec.toLong()} checksum=$checksum (warm=$warm)"
        )

        return BenchResult(name, iterations, elapsed, opsPerSec, checksum)
    }

    @Test
    public fun microbench_outcome_core_paths() {
        val results = mutableListOf<BenchResult>()

        // 1) Success pipeline: mapValue -> then -> filterOrElse
        results += runBench(name = "success-pipeline") { _ ->
            val start: Outcome<Int, String> = Outcome.success(1)
            val o: Outcome<Int, String> = start
                .mapValue { it + 41 }
                .then { v ->
                    if (v == 42) {
                        Outcome.success(v + 1) // covariant widen: Nothing -> String
                    } else {
                        Outcome.failure("nope")
                    }
                }
                .filterOrElse(predicate = { it > 40 }, onFailure = { "too small: $it" })

            when (o) {
                is Outcome.Success -> o.value.toLong()
                is Outcome.Failure -> 0L
            }
        }
        run {
            val start: Outcome<Int, String> = Outcome.success(1)
            val sanity: Outcome<Int, String> = start
                .mapValue { it + 41 }
                .then { v -> if (v == 42) Outcome.success(v + 1) else Outcome.failure("nope") }
                .filterOrElse(predicate = { it > 40 }, onFailure = { "too small: $it" })
            assertEquals(43, sanity.getOrThrow { IllegalStateException(it) })
        }

        // 2) Failure pipeline: mapError -> orElse fallback
        results += runBench(name = "failure-pipeline") { _ ->
            val base: Outcome<Int, String> = Outcome.failure("boom")
            val o: Outcome<Int, String> = base
                .mapError { "mapped-$it" }
                .orElse { _: String -> Outcome.success(123) }

            when (o) {
                is Outcome.Success -> o.value.toLong()
                is Outcome.Failure -> 0L
            }
        }
        run {
            val base: Outcome<Int, String> = Outcome.failure("boom")
            val sanity: Outcome<Int, String> = base
                .mapError { "mapped-$it" }
                .orElse { _: String -> Outcome.success(123) }
            assertEquals(123, sanity.getOrThrow { IllegalStateException(it) })
        }

        // 3) zip of two successes
        results += runBench(name = "zip-success-success") { _ ->
            val a: Outcome<Int, Nothing> = Outcome.success(10)
            val b: Outcome<Int, Nothing> = Outcome.success(32)
            val z = a.zip(b)
            when (z) {
                is Outcome.Success -> (z.value.first + z.value.second).toLong()
                is Outcome.Failure -> 0L
            }
        }
        run {
            val z = Outcome.success(10).zip(Outcome.success(32))
            val sum = when (z) {
                is Outcome.Success -> z.value.first + z.value.second
                is Outcome.Failure -> error("unexpected failure")
            }
            assertEquals(42, sum)
        }

        // 4) collectAll on list (all success)
        results += runBench(name = "collectAll-all-success") { _ ->
            val list = listOf(
                Outcome.success(1),
                Outcome.success(2),
                Outcome.success(3),
                Outcome.success(4),
                Outcome.success(5),
            )
            val c = list.collectAll()
            when (c) {
                is Outcome.Success -> c.value.sum().toLong()
                is Outcome.Failure -> 0L
            }
        }
        run {
            val c = listOf(Outcome.success(1), Outcome.success(2)).collectAll()
            assertTrue(c.isSuccess)
            val got = when (c) {
                is Outcome.Success -> c.value
                is Outcome.Failure -> emptyList()
            }
            assertEquals(listOf(1, 2), got)
        }

        // 5) firstSuccessOf across mixed results
        results += runBench(name = "firstSuccessOf-mixed") { _ ->
            val o = firstSuccessOf<Int, String>(
                Outcome.failure("a"),
                Outcome.failure("b"),
                Outcome.success(7),
                Outcome.success(9),
            )
            when (o) {
                is Outcome.Success -> (o.value * 3).toLong()
                is Outcome.Failure -> 0L
            }
        }
        run {
            val o = firstSuccessOf<Int, String>(Outcome.failure("x"), Outcome.success(7), Outcome.success(9))
            assertEquals(7, o.getOrThrow { IllegalStateException(it) })
        }

        // Minimal measurement sanity
        for (r in results) {
            assertTrue(r.elapsed.inWholeNanoseconds > 0, "elapsed should be > 0 for ${r.name}")
            assertTrue(r.opsPerSec > 0.0, "ops/sec should be > 0 for ${r.name}")
        }
    }
}
