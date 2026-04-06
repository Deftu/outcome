@file:JvmName("OutcomeSuspendExtensions")

package dev.deftu.outcome.coroutines

import dev.deftu.outcome.Outcome
import dev.deftu.outcome.Outcome.Companion.failure
import dev.deftu.outcome.Outcome.Failure
import dev.deftu.outcome.Outcome.Success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.JvmName

@OptIn(ExperimentalContracts::class)
public suspend fun <T> Outcome.Companion.attemptSuspend(block: suspend () -> T): Outcome<T, Throwable> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return try {
        success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        failure(e)
    }
}

@OptIn(ExperimentalContracts::class)
public suspend fun <T, E> Outcome.Companion.attemptSuspend(converter: suspend (Throwable) -> E, block: suspend () -> T): Outcome<T, E> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        callsInPlace(converter, InvocationKind.AT_MOST_ONCE)
    }

    return try {
        success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        failure(converter(e))
    }
}

@OptIn(ExperimentalContracts::class)
public suspend fun <T, E> Outcome.Companion.nullableSuspend(value: T?, onNull: suspend () -> E): Outcome<T, E> {
    contract {
        callsInPlace(onNull, InvocationKind.AT_MOST_ONCE)
    }

    return if (value != null) {
        success(value)
    } else {
        failure(onNull())
    }
}

@OptIn(ExperimentalContracts::class)
public suspend fun <E> Outcome.Companion.boolSuspend(condition: Boolean, onFalse: suspend () -> E): Outcome<Boolean, E> {
    contract {
        callsInPlace(onFalse, InvocationKind.AT_MOST_ONCE)
    }

    return if (condition) {
        success(condition)
    } else {
        failure(onFalse())
    }
}

@OptIn(ExperimentalContracts::class)
public suspend fun <T, E> Outcome<T, E>.getOrElseSuspend(mapper: suspend (E) -> T): T {
    contract {
        callsInPlace(mapper, InvocationKind.AT_MOST_ONCE)
    }

    return when (this) {
        is Success -> this.value
        is Failure -> mapper(this.error)
    }
}

@OptIn(ExperimentalContracts::class)
public suspend fun <T, E> Outcome<T, E>.orElseSuspend(mapper: suspend (E) -> Outcome<T, E>): Outcome<T, E> {
    contract {
        callsInPlace(mapper, InvocationKind.AT_MOST_ONCE)
    }

    return when (this) {
        is Success -> this
        is Failure -> mapper(this.error)
    }
}

@OptIn(ExperimentalContracts::class)
public suspend fun <T, E> Outcome<T, E>.filterOrElseSuspend(
    predicate: suspend (T) -> Boolean,
    onFailure: suspend (T) -> E
): Outcome<T, E> {
    contract {
        callsInPlace(predicate, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }

    return when (this) {
        is Success -> {
            if (predicate(this.value)) {
                this
            } else {
                failure(onFailure(this.value))
            }
        }

        is Failure -> this
    }
}

@OptIn(ExperimentalContracts::class)
public suspend fun <T, E, R> Outcome<T, E>.mapValueSuspend(transform: suspend (T) -> R): Outcome<R, E> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }

    return when (this) {
        is Success -> Outcome.success(transform(this.value))
        is Failure -> this
    }
}

@OptIn(ExperimentalContracts::class)
public suspend fun <T, E, F> Outcome<T, E>.mapErrorSuspend(transform: suspend (E) -> F): Outcome<T, F> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }

    return when (this) {
        is Success -> this
        is Failure -> failure(transform(this.error))
    }
}

@OptIn(ExperimentalContracts::class)
public suspend fun <T, E, R, F> Outcome<T, E>.bimapSuspend(
    transformValue: suspend (T) -> R,
    transformError: suspend (E) -> F
): Outcome<R, F> {
    contract {
        callsInPlace(transformValue, InvocationKind.AT_MOST_ONCE)
        callsInPlace(transformError, InvocationKind.AT_MOST_ONCE)
    }

    return when (this) {
        is Success -> Outcome.success(transformValue(this.value))
        is Failure -> failure(transformError(this.error))
    }
}

@OptIn(ExperimentalContracts::class)
public suspend fun <T, E, R> Outcome<T, E>.thenSuspend(transform: suspend (T) -> Outcome<R, E>): Outcome<R, E> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }

    return when (this) {
        is Success -> transform(this.value)
        is Failure -> this
    }
}

@OptIn(ExperimentalContracts::class)
public suspend fun <T, E, R> Outcome<T, E>.foldSuspend(
    onSuccess: suspend (T) -> R,
    onFailure: suspend (E) -> R
): R {
    contract {
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }

    return when (this) {
        is Success -> onSuccess(this.value)
        is Failure -> onFailure(this.error)
    }
}

@OptIn(ExperimentalContracts::class)
public suspend fun <T, E> Outcome<T, E>.onSuccessSuspend(action: suspend (T) -> Unit): Outcome<T, E> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }

    if (this is Success) {
        action(this.value)
    }

    return this
}

@OptIn(ExperimentalContracts::class)
public suspend fun <T, E> Outcome<T, E>.onFailureSuspend(action: suspend (E) -> Unit): Outcome<T, E> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }

    if (this is Failure) {
        action(this.error)
    }

    return this
}
