@file:JvmName("HttpResponseOutcomes")

package dev.deftu.outcome.ktor.client

import dev.deftu.outcome.Outcome
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.jvm.JvmName

/**
 * Converts a Ktor HttpResponse into an Outcome.
 * If the status is 2xx, it attempts to deserialize the body into [T].
 * If it's a non-2xx status, it returns a Failure with the raw HttpResponse for inspection.
 */
public suspend inline fun <reified T> HttpResponse.toOutcome(): Outcome<T, HttpResponse> {
    return if (status.isSuccess()) {
        try {
            Outcome.success(body<T>())
        } catch (e: Throwable) {
            // Serialization failed even though the request succeeded
            Outcome.failure(this)
        }
    } else {
        Outcome.failure(this)
    }
}

/**
 * A highly specific mapper: converts the HttpResponse into your Domain Error.
 */
public suspend inline fun <reified T, E> HttpResponse.toOutcome(
    crossinline errorMapper: suspend (HttpResponse) -> E
): Outcome<T, E> {
    return if (status.isSuccess()) {
        try {
            Outcome.success(body<T>())
        } catch (e: Throwable) {
            Outcome.failure(errorMapper(this))
        }
    } else {
        Outcome.failure(errorMapper(this))
    }
}
