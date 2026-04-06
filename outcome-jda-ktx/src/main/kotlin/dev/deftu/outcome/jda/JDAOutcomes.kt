@file:JvmName("JDAOutcomes")

package dev.deftu.outcome.jda

import dev.deftu.outcome.Outcome
import dev.deftu.outcome.coroutines.attemptSuspend
import dev.minn.jda.ktx.events.await
import kotlinx.coroutines.withTimeout
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import kotlin.time.Duration

public suspend inline fun <reified T : GenericEvent> JDA.awaitEventOutcome(
    timeout: Duration,
    crossinline filter: (T) -> Boolean = { true }
): Outcome<T, Throwable> {
    return Outcome.attemptSuspend {
        withTimeout(timeout) {
            this@awaitEventOutcome.await<T> { filter(it) }
        }
    }
}
