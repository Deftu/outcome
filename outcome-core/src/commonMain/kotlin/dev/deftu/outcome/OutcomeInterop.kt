@file:JvmName("OutcomeInterop")

package dev.deftu.outcome

import kotlin.jvm.JvmName

public fun <T, E> Pair<T?, E?>.toOutcome(): Outcome<T, E> {
    return Outcome.fromPair(this)
}

public fun <T, E> Result<T>.toOutcome(converter: (Throwable) -> E): Outcome<T, E> {
    return Outcome.fromResult(this, converter)
}

public fun <T> Result<T>.toOutcome(): Outcome<T, Throwable> {
    return Outcome.fromResult(this)
}
