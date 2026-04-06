@file:JvmName("ApplicationCallOutcomes")

package dev.deftu.outcome.ktor.server

import dev.deftu.outcome.Outcome
import dev.deftu.outcome.coroutines.attemptSuspend
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

public suspend inline fun <reified T : Any> ApplicationCall.receiveOutcome(): Outcome<T, Throwable> {
    return Outcome.attemptSuspend {
        receive<T>()
    }
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun <reified T : Any, E> ApplicationCall.receiveOutcome(
    crossinline errorMapper: suspend (Throwable) -> E
): Outcome<T, E> {
    contract {
        callsInPlace(errorMapper, InvocationKind.AT_MOST_ONCE)
    }

    return Outcome.attemptSuspend(
        converter = { errorMapper(it) }
    ) {
        receive<T>()
    }
}
