package dev.deftu.outcome.tests.vibed

import dev.deftu.outcome.Outcome
import dev.deftu.outcome.collectAll
import dev.deftu.outcome.partition
import dev.deftu.outcome.transpose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Batch_UserImport_Test {
    data class RawUser(val id: String?, val name: String?)
    data class User(val id: String, val name: String)

    private fun validate(raw: RawUser): Outcome<User, String> {
        return Outcome.nullable(raw.id) { "missing id" }
            .zip(Outcome.nullable(raw.name) { "missing name" })
            .mapValue { (id, name) -> id.trim() to name.trim() }
            .filterOrElse(
                predicate = { (id, name) -> id.isNotEmpty() && name.isNotEmpty() },
                onFailure = { (id, name) -> "blank field(s): id='$id' name='$name'" }
            )
            .mapValue { (id, name) -> User(id, name) }
            .onSuccess { println("[validate] OK=$it") }
            .onFailure { println("[validate] ERR=$it raw=$raw") }
    }

    @Test
    fun batch_UserImport() {
        val raws = listOf(
            RawUser("u1", "Sam"),
            RawUser(null, "NoId"),
            RawUser("  ", "BlankId"),
            RawUser("u4", "  "),
            RawUser("u5", "Alex")
        )

        val results = raws.map { validate(it) }

        val (values, errors) = results.partition()
        println("partition values=$values errors=$errors")
        assertEquals(listOf(User("u1", "Sam"), User("u5", "Alex")), values)
        assertEquals(3, errors.size)

        val collected = results.collectAll()
        println("collectAll=$collected")
        assertTrue(collected.isFailure)

        val keyed = mapOf(
            "r1" to validate(raws[0]),
            "r2" to validate(raws[1]),
            "r3" to validate(raws[4]),
        )

        val transposed = keyed.transpose()
        println("transpose=$transposed")
        assertTrue(transposed.isFailure)

        val okTransposed = mapOf("r1" to validate(raws[0]), "r3" to validate(raws[4])).transpose()
        println("okTranspose=$okTransposed")
        assertTrue(okTransposed.isSuccess)
        val map = okTransposed.getOrThrow { IllegalStateException(it) }
        assertEquals(setOf("r1", "r3"), map.keys)
        assertEquals("Sam", map["r1"]?.name)
        assertEquals("Alex", map["r3"]?.name)
    }
}
