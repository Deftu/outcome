@file:JvmName("OutcomeExtensions")

package dev.deftu.outcome

import kotlin.jvm.JvmName

public fun <T, R> T.attempt(block: T.() -> R): Outcome<R, Throwable> {
    return try {
        Outcome.success(block())
    } catch (e: Throwable) {
        Outcome.failure(e)
    }
}

public fun <T, R, E> T.attempt(converter: (Throwable) -> E, block: T.() -> R): Outcome<R, E> {
    return try {
        Outcome.success(block())
    } catch (e: Throwable) {
        Outcome.failure(converter(e))
    }
}

public fun <T, E> Outcome<T?, E>.ensureNotNull(onNull: () -> @UnsafeVariance E): Outcome<T, E> {
    return when (this) {
        is Outcome.Success -> this.value?.let { Outcome.success(it) } ?: Outcome.failure(onNull())
        is Outcome.Failure -> this
    }
}

public fun <T : Outcome<U, E>, U, E> Outcome<T, E>.flatten(): Outcome<U, E> {
    return when (this) {
        is Outcome.Success -> this.value
        is Outcome.Failure -> Outcome.failure(this.error)
    }
}
