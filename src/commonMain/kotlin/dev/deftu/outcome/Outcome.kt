package dev.deftu.outcome

import kotlin.jvm.JvmStatic

public sealed interface Outcome<out T, out E> {
    public companion object {
        @JvmStatic
        public fun <T> success(value: T): Outcome<T, Nothing> {
            return Success(value)
        }

        @JvmStatic
        public fun <E> failure(error: E): Outcome<Nothing, E> {
            return Failure(error)
        }

        @JvmStatic
        public fun <T> attempt(block: () -> T): Outcome<T, Throwable> {
            return try {
                success(block())
            } catch (e: Throwable) {
                failure(e)
            }
        }

        @JvmStatic
        public fun <T, E> attempt(converter: (Throwable) -> E, block: () -> T): Outcome<T, E> {
            return try {
                success(block())
            } catch (e: Throwable) {
                failure(converter(e))
            }
        }

        @JvmStatic
        public suspend fun <T> attemptSuspend(block: suspend () -> T): Outcome<T, Throwable> {
            return try {
                success(block())
            } catch (e: Throwable) {
                failure(e)
            }
        }

        @JvmStatic
        public suspend fun <T, E> attemptSuspend(converter: (Throwable) -> E, block: suspend () -> T): Outcome<T, E> {
            return try {
                success(block())
            } catch (e: Throwable) {
                failure(converter(e))
            }
        }

        @JvmStatic
        public fun <T, E> nullable(value: T?, onNull: () -> E): Outcome<T, E> {
            return if (value != null) {
                success(value)
            } else {
                failure(onNull())
            }
        }

        @JvmStatic
        public fun <T> nullable(value: T?): Outcome<T, Nothing?> {
            return if (value != null) {
                success(value)
            } else {
                failure(null)
            }
        }

        @JvmStatic
        public fun <E> bool(condition: Boolean, onFalse: () -> E): Outcome<Boolean, E> {
            return if (condition) {
                success(condition)
            } else {
                failure(onFalse())
            }
        }

        @JvmStatic
        public fun bool(condition: Boolean): Outcome<Boolean, Nothing?> {
            return if (condition) {
                success(condition)
            } else {
                failure(null)
            }
        }

        /**
         * Creates an [Outcome] from a [Pair].
         */
        @JvmStatic
        public fun <T, E> fromPair(pair: Pair<T?, E?>): Outcome<T, E> {
            val (value, error) = pair
            return when {
                value != null -> success(value)
                error != null -> failure(error)
                else -> throw IllegalArgumentException("Both value and error are null")
            }
        }

        /**
         * Creates an [Outcome] from a [Result].
         */
        @JvmStatic
        public fun <T, E> fromResult(result: Result<T>, mapper: (Throwable) -> E): Outcome<T, E> {
            return result.fold(
                onSuccess = { value -> success(value) },
                onFailure = { throwable -> failure(mapper(throwable)) }
            )
        }

        /**
         * Creates an [Outcome] from a [Result].
         */
        @JvmStatic
        public fun <T> fromResult(result: Result<T>): Outcome<T, Throwable> {
            return result.fold(
                onSuccess = { value -> success(value) },
                onFailure = { throwable -> failure(throwable) }
            )
        }
    }

    public class Success<T> internal constructor(public val value: T) : Outcome<T, Nothing> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success<*>) return false
            return value == other.value
        }

        override fun hashCode(): Int {
            return value?.hashCode() ?: 0
        }

        override fun toString(): String {
            return "Success($value)"
        }
    }

    public class Failure<E> internal constructor(public val error: E) : Outcome<Nothing, E> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Failure<*>) return false
            return error == other.error
        }

        override fun hashCode(): Int {
            return error?.hashCode() ?: 0
        }

        override fun toString(): String {
            return "Failure($error)"
        }
    }

    public val isSuccess: Boolean get() = this is Success
    public val isFailure: Boolean get() = this is Failure

    public operator fun component1(): T? {
        return when (this) {
            is Success -> this.value
            is Failure -> null
        }
    }

    public operator fun component2(): E? {
        return when (this) {
            is Success -> null
            is Failure -> this.error
        }
    }

    public fun getOrNull(): T? {
        return when (this) {
            is Success -> this.value
            is Failure -> null
        }
    }

    public fun getOrThrow(mapper: (E) -> Throwable): T {
        return when (this) {
            is Success -> this.value
            is Failure -> throw mapper(this.error)
        }
    }

    public fun getOrElse(mapper: (E) -> @UnsafeVariance T): T {
        return when (this) {
            is Success -> this.value
            is Failure -> mapper(this.error)
        }
    }

    public fun getOrDefault(defaultValue: @UnsafeVariance T): T {
        return when (this) {
            is Success -> this.value
            is Failure -> defaultValue
        }
    }

    public fun errorOrNull(): E? {
        return when (this) {
            is Success -> null
            is Failure -> this.error
        }
    }

    public fun orElse(mapper: (E) -> Outcome<@UnsafeVariance T, @UnsafeVariance E>): Outcome<T, E> {
        return when (this) {
            is Success -> this
            is Failure -> mapper(this.error)
        }
    }

    public fun filterOrElse(
        predicate: (T) -> Boolean,
        onFailure: (T) -> @UnsafeVariance E
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

    public fun <R> mapValue(transform: (T) -> R): Outcome<R, E> {
        return when (this) {
            is Success -> success(transform(this.value))
            is Failure -> this
        }
    }

    public fun <F> mapError(transform: (E) -> F): Outcome<T, F> {
        return when (this) {
            is Success -> this
            is Failure -> failure(transform(this.error))
        }
    }

    public fun <R, F> bimap(transformValue: (T) -> R, transformError: (E) -> F): Outcome<R, F> {
        return when (this) {
            is Success -> success(transformValue(this.value))
            is Failure -> failure(transformError(this.error))
        }
    }

    public fun <R> then(transform: (T) -> Outcome<R, @UnsafeVariance E>): Outcome<R, E> {
        return when (this) {
            is Success -> transform(this.value)
            is Failure -> this
        }
    }

    public fun <R> fold(onSuccess: (T) -> R, onFailure: (E) -> R): R {
        return when (this) {
            is Success -> onSuccess(this.value)
            is Failure -> onFailure(this.error)
        }
    }

    public fun <R> zip(other: Outcome<R, @UnsafeVariance E>): Outcome<Pair<T, R>, E> {
        return when (this) {
            is Success -> when (other) {
                is Success -> success(Pair(this.value, other.value))
                is Failure -> other
            }

            is Failure -> this
        }
    }

    public fun touchValue(action: Callback<@UnsafeVariance T>): Outcome<T, E> {
        return when (this) {
            is Success -> {
                action(this.value)
                this
            }

            is Failure -> this
        }
    }

    public fun touchError(action: Callback<@UnsafeVariance E>): Outcome<T, E> {
        return when (this) {
            is Success -> this
            is Failure -> {
                action(this.error)
                this
            }
        }
    }

    public fun unwrap(): T {
        return when (this) {
            is Success -> this.value
            is Failure -> throw IllegalStateException("Tried to unwrap a Failure(${this.error})")
        }
    }

    public fun expect(message: String): T {
        return when (this) {
            is Success -> this.value
            is Failure -> throw IllegalStateException("$message: ${this.error}")
        }
    }

    public fun asPair(): Pair<T?, E?> {
        return when (this) {
            is Success -> Pair(this.value, null)
            is Failure -> Pair(null, this.error)
        }
    }

    public fun asResult(converter: (E) -> Throwable): Result<T> {
        return when (this) {
            is Success -> Result.success(this.value)
            is Failure -> Result.failure(converter(this.error))
        }
    }
}
