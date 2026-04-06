@file:JvmName("OutcomeLogging")

package dev.deftu.outcome.logging

import dev.deftu.outcome.Outcome
import dev.deftu.outcome.isFailure
import dev.deftu.outcome.isSuccess
import org.slf4j.Logger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

public enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR
}

@OptIn(ExperimentalContracts::class)
public inline fun <T, E> Outcome<T, E>.logFailure(
    logger: Logger,
    level: LogLevel = LogLevel.ERROR,
    cause: Throwable? = null,
    message: (E) -> String
): Outcome<T, E> {
    contract {
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
    }

    if (this.isFailure()) {
        val isEnabled = when (level) {
            LogLevel.TRACE -> logger.isTraceEnabled
            LogLevel.DEBUG -> logger.isDebugEnabled
            LogLevel.INFO -> logger.isInfoEnabled
            LogLevel.WARN -> logger.isWarnEnabled
            LogLevel.ERROR -> logger.isErrorEnabled
        }

        if (isEnabled) {
            val msg = message(this.error)
            when (level) {
                LogLevel.TRACE -> logger.trace(msg, cause)
                LogLevel.DEBUG -> logger.debug(msg, cause)
                LogLevel.INFO -> logger.info(msg, cause)
                LogLevel.WARN -> logger.warn(msg, cause)
                LogLevel.ERROR -> logger.error(msg, cause)
            }
        }
    }
    return this
}

@OptIn(ExperimentalContracts::class)
public inline fun <T, E> Outcome<T, E>.logSuccess(
    logger: Logger,
    level: LogLevel = LogLevel.DEBUG,
    message: (T) -> String
): Outcome<T, E> {
    contract {
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
    }

    if (this.isSuccess()) {
        val isEnabled = when (level) {
            LogLevel.TRACE -> logger.isTraceEnabled
            LogLevel.DEBUG -> logger.isDebugEnabled
            LogLevel.INFO -> logger.isInfoEnabled
            LogLevel.WARN -> logger.isWarnEnabled
            LogLevel.ERROR -> logger.isErrorEnabled
        }

        if (isEnabled) {
            val msg = message(this.value)
            when (level) {
                LogLevel.TRACE -> logger.trace(msg)
                LogLevel.DEBUG -> logger.debug(msg)
                LogLevel.INFO -> logger.info(msg)
                LogLevel.WARN -> logger.warn(msg)
                LogLevel.ERROR -> logger.error(msg)
            }
        }
    }
    return this
}
