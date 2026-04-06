@file:JvmName("AkumaOutcomes")

package dev.deftu.outcome.jda.akuma

import dev.deftu.akuma.CommandContext
import dev.deftu.outcome.Outcome
import dev.deftu.outcome.coroutines.foldSuspend
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public inline fun <reified T> CommandContext.outcome(
    name: String,
    deserializer: (OptionMapping) -> T = CommandContext::defaultDeserializer
): Outcome<T, String> {
    val option = event.getOption(name)
    return if (option != null) {
        Outcome.success(deserializer(option))
    } else {
        Outcome.failure("Missing required option: $name")
    }
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun <T, E> CommandContext.replyOutcome(
    outcome: Outcome<T, E>,
    crossinline errorMessage: suspend (E) -> String,
    crossinline successMessage: suspend (T) -> String
) {
    contract {
        callsInPlace(errorMessage, InvocationKind.AT_MOST_ONCE)
        callsInPlace(successMessage, InvocationKind.AT_MOST_ONCE)
    }

    outcome.foldSuspend(
        onSuccess = { data ->
            event.reply(successMessage(data)).queue()
        },
        onFailure = { error ->
            event.reply(errorMessage(error))
                .setEphemeral(true)
                .queue()
        }
    )
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun <T, E> CommandContext.handleOutcome(
    outcome: Outcome<T, E>,
    crossinline onFailure: suspend CommandContext.(E) -> Unit,
    crossinline onSuccess: suspend CommandContext.(T) -> Unit
) {
    contract {
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
    }

    outcome.foldSuspend(
        onSuccess = { data -> onSuccess(data) },
        onFailure = { error -> onFailure(error) }
    )
}
