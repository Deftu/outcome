@file:JvmName("RestActionOutcomes")

package dev.deftu.outcome.jda

import dev.deftu.outcome.Outcome
import dev.deftu.outcome.coroutines.attemptSuspend
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.RestAction
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public class DiscordErrorMatcher<E> @PublishedApi internal constructor(
    private val fallback: suspend (Throwable) -> E
) {
    @PublishedApi
    internal val handlers: MutableMap<ErrorResponse, suspend (ErrorResponseException) -> E> = mutableMapOf()

    public fun on(error: ErrorResponse, handler: suspend (ErrorResponseException) -> E) {
        handlers[error] = handler
    }

    public fun on(vararg errors: ErrorResponse, handler: suspend (ErrorResponseException) -> E) {
        for (error in errors) {
            handlers[error] = handler
        }
    }

    @PublishedApi
    internal suspend fun handle(error: Throwable): E {
        if (error is ErrorResponseException) {
            val handler = handlers[error.errorResponse]
            if (handler != null) {
                return handler(error)
            }
        }
        return fallback(error)
    }
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun <T, E> RestAction<T>.awaitOutcome(
    noinline fallback: suspend (Throwable) -> E,
    crossinline matchers: DiscordErrorMatcher<E>.() -> Unit
): Outcome<T, E> {
    contract {
        callsInPlace(matchers, InvocationKind.EXACTLY_ONCE)
    }

    val matcher = DiscordErrorMatcher(fallback).apply(matchers)

    return Outcome.attemptSuspend(
        converter = { error -> matcher.handle(error) }
    ) {
        this.await()
    }
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun <T, E> RestAction<T>.ensure(
    condition: Boolean,
    crossinline onFailure: () -> E
): Outcome<T, E> {
    contract {
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }

    return if (condition) {
        this.awaitOutcome(
            fallback = { error -> onFailure() },
            matchers = {}
        )
    } else {
        Outcome.failure(onFailure())
    }
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun <T, E> RestAction<T>.requirePermission(
    member: Member,
    permission: Permission,
    crossinline onFailure: () -> E
): Outcome<T, E> {
    contract {
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }

    return ensure(member.hasPermission(permission), onFailure)
}
