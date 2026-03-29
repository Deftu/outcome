@file:JvmName("OutcomeCollections")

package dev.deftu.outcome

import kotlin.jvm.JvmName

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
