@file:JvmName("ApplicationCallOutcomes")

package dev.deftu.outcome.ktor.server

import dev.deftu.outcome.Outcome
import dev.deftu.outcome.coroutines.attemptSuspend
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlin.jvm.JvmName

/**
 * Safely attempts to receive and deserialize the request body.
 */
public suspend inline fun <reified T : Any> ApplicationCall.receiveOutcome(): Outcome<T, Throwable> {
    return Outcome.attemptSuspend {
        receive<T>()
    }
}

/**
 * Safely receives the request body, mapping the serialization error to your domain error.
 */
public suspend inline fun <reified T : Any, E> ApplicationCall.receiveOutcome(
    crossinline errorMapper: suspend (Throwable) -> E
): Outcome<T, E> {
    return Outcome.attemptSuspend(
        converter = { errorMapper(it) }
    ) {
        receive<T>()
    }
}
