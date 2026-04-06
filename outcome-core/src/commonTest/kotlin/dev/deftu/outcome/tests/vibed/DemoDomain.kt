package dev.deftu.outcome.tests.vibed

import dev.deftu.outcome.Outcome

internal object DemoDomain {
    data class ServiceProps(val host: String, val port: Int)

    fun parseProperties(text: String): Outcome<Map<String, String>, String> {
        println("[parseProperties] raw=\n$text")
        val pairs = mutableListOf<Pair<String, String>>()

        for (raw in text.lineSequence()) {
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue

            val idx = line.indexOf('=')
            if (idx <= 0 || idx >= line.lastIndex) {
                return Outcome.failure("Malformed line: \"$line\"")
            }

            val k = line.take(idx).trim()
            val v = line.substring(idx + 1).trim()
            pairs += k to v
        }

        if (pairs.isEmpty()) return Outcome.failure("No properties found")

        val map = pairs.toMap()
        println("[parseProperties] map=$map")
        return Outcome.success(map)
    }

    fun assembleServiceProps(map: Map<String, String>): Outcome<ServiceProps, String> {
        return Outcome
            .nullable(map["host"]) { "Missing 'host'" }
            .zip(Outcome.nullable(map["port"]) { "Missing 'port'" })
            .mapValue { (host, portStr) -> host to portStr }
            .then { (host, portStr) ->
                Outcome.attempt({ "Invalid port: $portStr" }) {
                    val port = portStr.toInt()
                    ServiceProps(host, port)
                }
            }
            .filterOrElse(
                predicate = { it.port in 1..65535 },
                onFailure = { "Port out of range: ${it.port}" }
            )
            .onSuccess { println("[assembleServiceProps] OK=$it") }
            .onFailure { println("[assembleServiceProps] ERR=$it") }
    }

    fun openSocketLike(host: String, port: Int): Outcome<String, String> {
        return when {
            host == "localhost" && port == 0 -> Outcome.failure("Refused: reserved port")
            host.contains("invalid") -> Outcome.failure("DNS failure")
            port == 13 -> Outcome.failure("Firewall drop")
            else -> Outcome.success("CONNECTED($host:$port)")
        }.also { println("[openSocketLike] -> $it") }
    }
}
