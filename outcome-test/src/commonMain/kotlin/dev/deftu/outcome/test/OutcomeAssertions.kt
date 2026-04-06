@file:JvmName("OutcomeAssertions")

package dev.deftu.outcome.test

import dev.deftu.outcome.Outcome
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

@OptIn(ExperimentalContracts::class)
public fun <T, E> Outcome<T, E>.shouldBeSuccess(): T {
    contract {
        returns() implies(this@shouldBeSuccess is Outcome.Success)
    }

    return when (this) {
        is Outcome.Success -> this.value
        is Outcome.Failure -> throw AssertionError("Expected Outcome.Success but was Outcome.Failure.\nError details: ${this.error}")
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <T, E> Outcome<T, E>.shouldBeSuccess(assertions: (T) -> Unit) {
    contract {
        callsInPlace(assertions, InvocationKind.EXACTLY_ONCE)
        returns() implies(this@shouldBeSuccess is Outcome.Success)
    }

    val value = this.shouldBeSuccess()
    assertions(value)
}

@OptIn(ExperimentalContracts::class)
public fun <T, E> Outcome<T, E>.shouldBeFailure(): E {
    contract {
        returns() implies(this@shouldBeFailure is Outcome.Failure)
    }

    return when (this) {
        is Outcome.Failure -> this.error
        is Outcome.Success -> throw AssertionError("Expected Outcome.Failure but was Outcome.Success.\nValue details: ${this.value}")
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <T, E> Outcome<T, E>.shouldBeFailure(assertions: (E) -> Unit) {
    contract {
        callsInPlace(assertions, InvocationKind.EXACTLY_ONCE)
        returns() implies(this@shouldBeFailure is Outcome.Failure)
    }

    val error = this.shouldBeFailure()
    assertions(error)
}

@OptIn(ExperimentalContracts::class)
public fun <T, E> Outcome<T, E>.shouldBeSuccess(expected: T) {
    contract {
        returns() implies(this@shouldBeSuccess is Outcome.Success)
    }

    val actual = this.shouldBeSuccess()
    if (actual != expected) {
        throw AssertionError("Expected Success value to be <$expected> but was <$actual>.")
    }
}

@OptIn(ExperimentalContracts::class)
public fun <T, E> Outcome<T, E>.shouldBeFailure(expected: E) {
    contract {
        returns() implies(this@shouldBeFailure is Outcome.Failure)
    }

    val actual = this.shouldBeFailure()
    if (actual != expected) {
        throw AssertionError("Expected Failure error to be <$expected> but was <$actual>.")
    }
}
