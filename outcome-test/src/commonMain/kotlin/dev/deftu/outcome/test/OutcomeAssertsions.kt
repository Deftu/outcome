@file:JvmName("OutcomeAssertions")

package dev.deftu.outcome.test

import dev.deftu.outcome.Outcome
import kotlin.jvm.JvmName

/**
 * Asserts that the Outcome is a [Outcome.Success] and returns the unwrapped value.
 * @throws AssertionError if the Outcome is a Failure.
 */
public fun <T, E> Outcome<T, E>.shouldBeSuccess(): T {
    return when (this) {
        is Outcome.Success -> this.value
        is Outcome.Failure -> throw AssertionError(
            "Expected Outcome.Success but was Outcome.Failure.\nError details: ${this.error}"
        )
    }
}

/**
 * Asserts that the Outcome is a [Outcome.Success] and executes the provided [assertions] block on the value.
 */
public inline fun <T, E> Outcome<T, E>.shouldBeSuccess(assertions: (T) -> Unit) {
    val value = this.shouldBeSuccess()
    assertions(value)
}

/**
 * Asserts that the Outcome is a [Outcome.Failure] and returns the unwrapped error.
 * @throws AssertionError if the Outcome is a Success.
 */
public fun <T, E> Outcome<T, E>.shouldBeFailure(): E {
    return when (this) {
        is Outcome.Failure -> this.error
        is Outcome.Success -> throw AssertionError(
            "Expected Outcome.Failure but was Outcome.Success.\nValue details: ${this.value}"
        )
    }
}

/**
 * Asserts that the Outcome is a [Outcome.Failure] and executes the provided [assertions] block on the error.
 */
public inline fun <T, E> Outcome<T, E>.shouldBeFailure(assertions: (E) -> Unit) {
    val error = this.shouldBeFailure()
    assertions(error)
}

/**
 * (Optional but highly recommended) 
 * Asserts that the Outcome is a Success AND the value exactly matches the [expected] value.
 */
public fun <T, E> Outcome<T, E>.shouldBeSuccess(expected: T) {
    val actual = this.shouldBeSuccess()
    if (actual != expected) {
        throw AssertionError("Expected Success value to be <$expected> but was <$actual>.")
    }
}

/**
 * (Optional but highly recommended) 
 * Asserts that the Outcome is a Failure AND the error exactly matches the [expected] error.
 */
public fun <T, E> Outcome<T, E>.shouldBeFailure(expected: E) {
    val actual = this.shouldBeFailure()
    if (actual != expected) {
        throw AssertionError("Expected Failure error to be <$expected> but was <$actual>.")
    }
}
