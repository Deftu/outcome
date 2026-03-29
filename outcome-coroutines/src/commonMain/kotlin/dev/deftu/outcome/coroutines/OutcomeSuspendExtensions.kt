@file:JvmName("OutcomeSuspendExtensions")

package dev.deftu.outcome.coroutines

import dev.deftu.outcome.Outcome
import dev.deftu.outcome.Outcome.Companion.failure
import dev.deftu.outcome.Outcome.Failure
import dev.deftu.outcome.Outcome.Success
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

public suspend fun <T> Outcome.Companion.attemptSuspend(block: suspend () -> T): Outcome<T, Throwable> {
    return try {
        success(block())
    } catch (e: Throwable) {
        failure(e)
    }
}

public suspend fun <T, E> Outcome.Companion.attemptSuspend(converter: suspend (Throwable) -> E, block: suspend () -> T): Outcome<T, E> {
    return try {
        success(block())
    } catch (e: Throwable) {
        failure(converter(e))
    }
}

public suspend fun <T, E> Outcome.Companion.nullableSuspend(value: T?, onNull: suspend () -> E): Outcome<T, E> {
    return if (value != null) {
        success(value)
    } else {
        failure(onNull())
    }
}

public suspend fun <E> Outcome.Companion.boolSuspend(condition: Boolean, onFalse: suspend () -> E): Outcome<Boolean, E> {
    return if (condition) {
        success(condition)
    } else {
        failure(onFalse())
    }
}

public suspend fun <T, E> Outcome<T, E>.getOrElseSuspend(mapper: suspend (E) -> T): T {
    return when (this) {
        is Success -> this.value
        is Failure -> mapper(this.error)
    }
}

public suspend fun <T, E> Outcome<T, E>.orElseSuspend(mapper: suspend (E) -> Outcome<T, E>): Outcome<T, E> {
    return when (this) {
        is Success -> this
        is Failure -> mapper(this.error)
    }
}

public suspend fun <T, E> Outcome<T, E>.filterOrElseSuspend(
    predicate: suspend (T) -> Boolean,
    onFailure: suspend (T) -> E
): Outcome<T, E> {
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

public suspend fun <T, E, R> Outcome<T, E>.mapValueSuspend(transform: suspend (T) -> R): Outcome<R, E> {
    return when (this) {
        is Success -> Outcome.Companion.success(transform(this.value))
        is Failure -> this
    }
}

public suspend fun <T, E, F> Outcome<T, E>.mapErrorSuspend(transform: suspend (E) -> F): Outcome<T, F> {
    return when (this) {
        is Success -> this
        is Failure -> failure(transform(this.error))
    }
}

public suspend fun <T, E, R, F> Outcome<T, E>.bimapSuspend(
    transformValue: suspend (T) -> R,
    transformError: suspend (E) -> F
): Outcome<R, F> {
    return when (this) {
        is Success -> Outcome.Companion.success(transformValue(this.value))
        is Failure -> failure(transformError(this.error))
    }
}

public suspend fun <T, E, R> Outcome<T, E>.thenSuspend(transform: suspend (T) -> Outcome<R, E>): Outcome<R, E> {
    return when (this) {
        is Success -> transform(this.value)
        is Failure -> this
    }
}

public suspend fun <T, E, R> Outcome<T, E>.foldSuspend(
    onSuccess: suspend (T) -> R,
    onFailure: suspend (E) -> R
): R {
    return when (this) {
        is Success -> onSuccess(this.value)
        is Failure -> onFailure(this.error)
    }
}

public suspend fun <T, E> Outcome<T, E>.touchValueSuspend(action: suspend (T) -> Unit): Outcome<T, E> {
    return when (this) {
        is Success -> {
            action(this.value)
            this
        }

        is Failure -> this
    }
}

public suspend fun <T, E> Outcome<T, E>.touchErrorSuspend(action: suspend (E) -> Unit): Outcome<T, E> {
    return when (this) {
        is Success -> this
        is Failure -> {
            action(this.error)
            this
        }
    }
}
