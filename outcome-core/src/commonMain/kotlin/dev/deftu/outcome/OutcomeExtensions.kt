@file:JvmName("OutcomeExtensions")

package dev.deftu.outcome

import dev.deftu.outcome.Outcome.Failure
import dev.deftu.outcome.Outcome.Success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

@OptIn(ExperimentalContracts::class)
public fun <T, R> T.attempt(block: T.() -> R): Outcome<R, Throwable> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return try {
        val result = block()
        Outcome.success(result)
    } catch (e: Throwable) {
        Outcome.failure(e)
    }
}

@OptIn(ExperimentalContracts::class)
public fun <T, R, E> T.attempt(converter: (Throwable) -> E, block: T.() -> R): Outcome<R, E> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        callsInPlace(converter, InvocationKind.AT_MOST_ONCE)
    }

    return try {
        val result = block()
        Outcome.success(result)
    } catch (e: Throwable) {
        Outcome.failure(converter(e))
    }
}

@OptIn(ExperimentalContracts::class)
public fun <T, E> Outcome<T, E>.isSuccess(): Boolean {
    contract {
        returns(true) implies(this@isSuccess is Success)
        returns(false) implies(this@isSuccess is Failure)
    }

    return this is Success
}

@OptIn(ExperimentalContracts::class)
public fun <T, E> Outcome<T, E>.isFailure(): Boolean {
    contract {
        returns(true) implies(this@isFailure is Failure)
        returns(false) implies(this@isFailure is Success)
    }

    return this is Failure
}

@OptIn(ExperimentalContracts::class)
public fun <T, E> Outcome<T?, E>.ensureNotNull(onNull: () -> @UnsafeVariance E): Outcome<T, E> {
    contract {
        callsInPlace(onNull, InvocationKind.AT_MOST_ONCE)
        returnsNotNull()
    }

    return when (this) {
        is Success -> this.value?.let { Outcome.success(it) } ?: Outcome.failure(onNull())
        is Failure -> this
    }
}

public fun <T : Outcome<U, E>, U, E> Outcome<T, E>.flatten(): Outcome<U, E> {
    return when (this) {
        is Success -> this.value
        is Failure -> Outcome.failure(this.error)
    }
}
