@file:JvmName("HttpClientOutcomes")

package dev.deftu.outcome.ktor.client

import dev.deftu.outcome.Outcome
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlin.jvm.JvmName

/**
 * Executes a Ktor Client request safely, catching network exceptions and mapping HTTP errors.
 */
public suspend inline fun <reified T, E> HttpClient.requestOutcome(
    crossinline requestBuilder: HttpRequestBuilder.() -> Unit,
    crossinline errorMapper: suspend (Throwable?, HttpResponse?) -> E
): Outcome<T, E> {
    return try {
        val response = request(requestBuilder)
        if (response.status.isSuccess()) {
            Outcome.success(response.body<T>())
        } else {
            Outcome.failure(errorMapper(null, response))
        }
    } catch (e: Throwable) {
        // Network failure, timeout, or serialization error
        Outcome.failure(errorMapper(e, null))
    }
}
