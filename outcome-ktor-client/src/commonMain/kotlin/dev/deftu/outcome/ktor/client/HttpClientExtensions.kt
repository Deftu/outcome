@file:JvmName("HttpClientOutcomes")

package dev.deftu.outcome.ktor.client

import dev.deftu.outcome.Outcome
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.utils.io.CancellationException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

@OptIn(ExperimentalContracts::class)
public suspend inline fun <reified T, E> HttpClient.requestOutcome(
    crossinline requestBuilder: HttpRequestBuilder.() -> Unit,
    crossinline errorMapper: suspend (Throwable?, HttpResponse?) -> E
): Outcome<T, E> {
    contract {
        callsInPlace(requestBuilder, InvocationKind.UNKNOWN)
        callsInPlace(errorMapper, InvocationKind.AT_MOST_ONCE)
    }

    return try {
        val response = request(requestBuilder)
        if (response.status.isSuccess()) {
            Outcome.success(response.body<T>())
        } else {
            Outcome.failure(errorMapper(null, response))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Outcome.failure(errorMapper(e, null))
    }
}
