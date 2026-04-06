@file:JvmName("OutcomeFlowExtensions")

package dev.deftu.outcome.coroutines

import dev.deftu.outcome.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.takeWhile
import kotlin.jvm.JvmName

public fun <T, E> Flow<Outcome<T, E>>.filterSuccess(): Flow<T> {
    return this.mapNotNull { it.getOrNull() }
}

public fun <T, E> Flow<Outcome<T, E>>.filterFailure(): Flow<E> {
    return this.mapNotNull { it.errorOrNull() }
}

public fun <T, E, R> Flow<Outcome<T, E>>.mapOutcomeValue(transform: suspend (T) -> R): Flow<Outcome<R, E>> {
    return this.map { outcome ->
        outcome.mapValueSuspend(transform)
    }
}

public fun <T, E, F> Flow<Outcome<T, E>>.mapOutcomeError(transform: suspend (E) -> F): Flow<Outcome<T, F>> {
    return this.map { outcome ->
        outcome.mapErrorSuspend(transform)
    }
}

public fun <T> Flow<T>.catchAsOutcome(): Flow<Outcome<T, Throwable>> {
    return this
        .map<T, Outcome<T, Throwable>> { Outcome.success(it) }
        .catch { emit(Outcome.failure(it)) }
}

public fun <T, E> Flow<T>.catchAsOutcome(converter: suspend (Throwable) -> E): Flow<Outcome<T, E>> {
    return this
        .map<T, Outcome<T, E>> { Outcome.success(it) }
        .catch { emit(Outcome.failure(converter(it))) }
}

public fun <T, E> Flow<Outcome<T, E>>.takeWhileSuccess(): Flow<T> {
    return this
        .takeWhile { it is Outcome.Success }
        .map { (it as Outcome.Success).value }
}

public fun <T, E> Flow<Outcome<T, E>>.takeWhileSuccess(onHalt: suspend (E) -> Unit): Flow<T> {
    return this
        .takeWhile { outcome ->
            if (outcome is Outcome.Failure) {
                onHalt(outcome.error)
                false
            } else {
                true
            }
        }
        .map { (it as Outcome.Success).value }
}
