@file:JvmName("HttpResponseOutcomes")

package dev.deftu.outcome.ktor.client

import dev.deftu.outcome.Outcome
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.CancellationException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

public suspend inline fun <reified T> HttpResponse.toOutcome(): Outcome<T, HttpResponse> {
    return if (status.isSuccess()) {
        try {
            Outcome.success(body<T>())
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            Outcome.failure(this)
        }
    } else {
        Outcome.failure(this)
    }
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun <reified T, E> HttpResponse.toOutcome(
    crossinline errorMapper: suspend (HttpResponse) -> E
): Outcome<T, E> {
    contract {
        callsInPlace(errorMapper, InvocationKind.AT_MOST_ONCE)
    }

    return if (status.isSuccess()) {
        try {
            Outcome.success(body<T>())
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            Outcome.failure(errorMapper(this))
        }
    } else {
        Outcome.failure(errorMapper(this))
    }
}
