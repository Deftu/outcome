@file:JvmName("OutcomeResources")

package dev.deftu.outcome

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

@OptIn(ExperimentalContracts::class)
@Suppress("UNCHECKED_CAST")
public fun <R : AutoCloseable, T, E> Outcome<R, E>.use(
    errorConverter: (Throwable) -> E,
    block: (R) -> Outcome<T, E>
): Outcome<T, E> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        callsInPlace(errorConverter, InvocationKind.AT_MOST_ONCE)
    }

    return then {
        try {
            val result = block(it)
            try {
                it.close()
                result
            } catch (_: Throwable) {
                result // Standard Kotlin 'use' ignores close exceptions if the block succeeded
            }
        } catch (t: Throwable) {
            try {
                it.close()
            } catch (_: Throwable) {
                // Suppress close exception
            }

            Outcome.failure(errorConverter(t))
        }
    }
}

@OptIn(ExperimentalContracts::class)
public fun <R : AutoCloseable, T> Outcome<R, Throwable>.use(block: (R) -> Outcome<T, Throwable>): Outcome<T, Throwable> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return use({ it }, block)
}
