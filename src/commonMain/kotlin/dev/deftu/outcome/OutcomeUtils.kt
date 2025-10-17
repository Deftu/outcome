@file:JvmName("OutcomeUtils")

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

public fun <T, E> firstSuccessOf(vararg outcomes: Outcome<T, E>): Outcome<T, E> {
    var lastFailure: Outcome.Failure<E>? = null
    for (outcome in outcomes) {
        when (outcome) {
            is Outcome.Success -> return outcome
            is Outcome.Failure -> lastFailure = outcome
        }
    }

    return lastFailure ?: throw IllegalArgumentException("No outcomes provided")
}

public fun <T, E> Outcome<T?, E>.ensureNotNull(onNull: () -> @UnsafeVariance E): Outcome<T, E> {
    return when (this) {
        is Outcome.Success -> this.value?.let { Outcome.success(it) } ?: Outcome.failure(onNull())
        is Outcome.Failure -> this
    }
}

public fun <T, E> Iterable<Outcome<T, E>>.collectAll(): Outcome<List<T>, E> {
    val values = mutableListOf<T>()
    for (outcome in this) {
        when (outcome) {
            is Outcome.Success -> values.add(outcome.value)
            is Outcome.Failure -> return Outcome.failure(outcome.error)
        }
    }

    return Outcome.success(values)
}

public fun <T, E> Iterable<Outcome<T, E>>.partition(): Pair<List<T>, List<E>> {
    val values = mutableListOf<T>()
    val errors = mutableListOf<E>()
    for (outcome in this) {
        when (outcome) {
            is Outcome.Success -> values.add(outcome.value)
            is Outcome.Failure -> errors.add(outcome.error)
        }
    }

    return values to errors
}

public fun <K, V, E> Map<K, Outcome<V, E>>.transpose(): Outcome<Map<K, V>, E> {
    val result = mutableMapOf<K, V>()
    for ((key, outcome) in this) {
        when (outcome) {
            is Outcome.Success -> result[key] = outcome.value
            is Outcome.Failure -> return Outcome.failure(outcome.error)
        }
    }

    return Outcome.success(result)
}

public fun <T : Outcome<U, E>, U, E> Outcome<T, E>.flatten(): Outcome<U, E> {
    return when (this) {
        is Outcome.Success -> this.value
        is Outcome.Failure -> Outcome.failure(this.error)
    }
}

@Suppress("UNCHECKED_CAST")
public fun <R : AutoCloseable, T, E> Outcome<R, E>.use(block: (R) -> Outcome<T, E>): Outcome<T, E> {
    return then {
        try {
            val result = block(it)
            try {
                it.close()
                result
            } catch (t: Throwable) {
                result
            }
        } catch (t: Throwable) {
            try {
                it.close()
            } catch (ignored: Throwable) {
                // Suppress close exception
            }

            Outcome.failure(t as E)
        }
    }
}

public fun <T, E> Pair<T?, E?>.toOutcome(): Outcome<T, E> {
    return Outcome.fromPair(this)
}

public fun <T, E> Result<T>.toOutcome(converter: (Throwable) -> E): Outcome<T, E> {
    return Outcome.fromResult(this, converter)
}

public fun <T> Result<T>.toOutcome(): Outcome<T, Throwable> {
    return Outcome.fromResult(this)
}
