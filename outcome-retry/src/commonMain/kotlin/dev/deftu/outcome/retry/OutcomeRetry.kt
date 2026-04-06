@file:JvmName("OutcomeRetry")

package dev.deftu.outcome.retry

import dev.deftu.outcome.Outcome
import dev.deftu.outcome.isSuccess
import kotlinx.coroutines.delay
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalContracts::class)
public suspend inline fun <T, E> retryOutcome(
    maxAttempts: Int = 3,
    initialDelay: Duration = 100.milliseconds,
    maxDelay: Duration = 10000.milliseconds,
    backoffFactor: Double = 2.0,
    retryIf: (E) -> Boolean = { true },
    block: suspend () -> Outcome<T, E>
): Outcome<T, E> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    require(maxAttempts > 0) { "maxAttempts must be at least 1" }

    var currentDelay = initialDelay
    var attempt = 1

    while (true) {
        val result = block()

        if (result.isSuccess()) {
            return result
        }

        if (attempt >= maxAttempts || !retryIf(result.error)) {
            return result
        }

        delay(currentDelay)

        attempt++
        currentDelay = (currentDelay * backoffFactor).coerceAtMost(maxDelay)
    }
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun <T, E> retryOutcomeFixed(
    maxAttempts: Int = 3,
    delay: Duration = 500.milliseconds,
    retryIf: (E) -> Boolean = { true },
    block: suspend () -> Outcome<T, E>
): Outcome<T, E> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    return retryOutcome(
        maxAttempts = maxAttempts,
        initialDelay = delay,
        maxDelay = delay,
        backoffFactor = 1.0,
        retryIf = retryIf,
        block = block
    )
}
