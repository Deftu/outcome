@file:JvmName("OutcomeLogging")

package dev.deftu.outcome.logging

import dev.deftu.outcome.Outcome
import org.slf4j.Logger
import kotlin.jvm.JvmName

public enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR
}

/**
 * Logs the error if the Outcome is a Failure, then returns the unmodified Outcome.
 * * @param logger The SLF4J Logger instance.
 * @param level The severity level to log at (default is ERROR).
 * @param cause An optional Throwable to pass to the logger for stack traces.
 * @param message A lambda that generates the log message from the error.
 */
public inline fun <T, E> Outcome<T, E>.logFailure(
    logger: Logger,
    level: LogLevel = LogLevel.ERROR,
    cause: Throwable? = null,
    crossinline message: (E) -> String
): Outcome<T, E> {
    if (this is Outcome.Failure) {
        val msg = message(this.error)
        when (level) {
            LogLevel.TRACE -> if (logger.isTraceEnabled) logger.trace(msg, cause)
            LogLevel.DEBUG -> if (logger.isDebugEnabled) logger.debug(msg, cause)
            LogLevel.INFO -> if (logger.isInfoEnabled) logger.info(msg, cause)
            LogLevel.WARN -> if (logger.isWarnEnabled) logger.warn(msg, cause)
            LogLevel.ERROR -> if (logger.isErrorEnabled) logger.error(msg, cause)
        }
    }
    return this
}

/**
 * Logs the value if the Outcome is a Success, then returns the unmodified Outcome.
 * * @param logger The SLF4J Logger instance.
 * @param level The severity level to log at (default is DEBUG).
 * @param message A lambda that generates the log message from the value.
 */
public inline fun <T, E> Outcome<T, E>.logSuccess(
    logger: Logger,
    level: LogLevel = LogLevel.DEBUG,
    crossinline message: (T) -> String
): Outcome<T, E> {
    if (this is Outcome.Success) {
        val msg = message(this.value)
        when (level) {
            LogLevel.TRACE -> if (logger.isTraceEnabled) logger.trace(msg)
            LogLevel.DEBUG -> if (logger.isDebugEnabled) logger.debug(msg)
            LogLevel.INFO -> if (logger.isInfoEnabled) logger.info(msg)
            LogLevel.WARN -> if (logger.isWarnEnabled) logger.warn(msg)
            LogLevel.ERROR -> if (logger.isErrorEnabled) logger.error(msg)
        }
    }
    return this
}
