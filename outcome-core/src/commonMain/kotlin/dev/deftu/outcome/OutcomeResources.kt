@file:JvmName("OutcomeResources")

package dev.deftu.outcome

import kotlin.jvm.JvmName

@Suppress("UNCHECKED_CAST")
public fun <R : AutoCloseable, T, E> Outcome<R, E>.use(block: (R) -> Outcome<T, E>): Outcome<T, E> {
    return then {
        try {
            val result = block(it)
            try {
                it.close()
                result
            } catch (t: Throwable) {
                result
            }
        } catch (t: Throwable) {
            try {
                it.close()
            } catch (ignored: Throwable) {
                // Suppress close exception
            }

            Outcome.failure(t as E)
        }
    }
}
