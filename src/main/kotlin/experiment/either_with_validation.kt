package experiment

import arrow.core.*
import arrow.core.computations.either
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.fx.coroutines.parTraverseEither
import com.clojj.birthdaykata.domain.ValidationResult
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

fun main() {

    val scope = CoroutineScope(SupervisorJob())

    scope.launch {
        programWithActions()
    }

    scope.launch {
        program()
    }

    Thread.sleep(1000)
}

// ---

// https://arrow-kt.io/docs/next/apidocs/arrow-fx-stm/arrow.fx.stm/-s-t-m/index.html
/*
fun STM.transfer(from: TVar<Int>, to: TVar<Int>, amount: Int): Unit {
    withdraw(from, amount)
    deposit(to, amount)
}

fun STM.deposit(acc: TVar<Int>, amount: Int): Unit {
    val current = acc.read()
    acc.write(current + amount)
    // or the shorthand acc.modify { it + amount }
}

fun STM.withdraw(acc: TVar<Int>, amount: Int): Unit {
    val current = acc.read()
    if (current - amount >= 0) acc.write(current + amount)
    else throw IllegalStateException("Not enough money in the account!")
}
*/

// ---

typealias Action<A> = suspend () -> A

fun createFetchAction(uuid: UUID): Action<Either<Nel<String>, String>> =
    { "result for $uuid".right() }

suspend fun programWithActions() {

    val uuids = listOf(UUID.randomUUID(), UUID.randomUUID())

    val acts = uuids.map { createFetchAction(it) }

    val result = either<Nel<String>, String> {
        // "list of IO actions"
        acts[0]()()
    }
    println(result)
}

// ---

private suspend fun program() {

    val uuids = listOf(UUID.randomUUID(), UUID.randomUUID())

    val results = either<Nel<String>, List<Int>> {
        // TODO validate many fields
        val validated = validateComposite("abc")()

        // "list of IO actions"
        val acts = uuids.map { createFetchAction(it) }

        val r = acts[0]()
/*
        val result = either<Nel<String>, List<Int>> {
            val a = fetchCatching(validated[0])()
            val b = fetchCatching(validated[1])()
            val c = fetchCatching(validated[2])()
            a + b + c
        }()
        result
*/
        // https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/debugging.md#debug-mode
        val result = validated.toList().parTraverseEither(Dispatchers.IO + CoroutineName("io-dispatchers")) {
            val list = fetch(it)
            list
        }.map { it.flatten() }()
        result

    }
    println(results)
}

suspend fun fetch(id: Char): Either<Nel<String>, List<Int>> {
    println("fetching $id on thread ${Thread.currentThread().name}...")
    return when (id) {
        'a' -> {
            listOf(1, 10).right()
        }
        'b' -> {
            delay(200)
            // listOf(2, 20).right()
            throw IOException("outage for 'b'")
        }
        'c' -> {
            delay(500)
            listOf(3, 30).right().also {
                println("finished c")
            }
        }

        else -> throw IllegalArgumentException("unknown id $id")
    }
}

suspend fun fetchCatching(id: Char): Either<Nel<String>, List<Int>> {
    println("fetching $id ...")
    return Either.catch {
        when (id) {
            'a' -> {
                listOf(1, 10)
            }
            'b' -> {
                delay(200)
                // listOf(2, 20)
                throw IOException("outage for 'b'")
            }
            'c' -> {
                delay(500)
                listOf(3, 30).also {
                    println("finished c")
                }
            }

            else -> throw IllegalArgumentException("unknown id $id")
        }
    }.mapLeft { Nel.fromListUnsafe(listOf(it.message ?: "TODO")) }
}

private fun validateComposite(name: String?): ValidationResult<String> {
    val fromNullable = ValidationResult.fromNullable(name) { "String must not be blank or null".nel() }
    return fromNullable.fold({ it.invalid() }) { nonNullName ->
        validateN(validateLowercase(nonNullName), validateMin3(nonNullName)).map { nonNullName }
    }
}

private fun validateLowercase(value: String): ValidationResult<String> =
    if (value.all { it.isLowerCase() }) value.valid()
    else "String must only contain lowercase, found '${value}'".invalidNel()

private fun validateMin3(value: String): ValidationResult<String> =
    if (value.length >= 3) value.valid()
    else "String must have at least 3 characters, found ${value.length}".invalidNel()


// helpers

fun <A, B> validateN(
    vrA: ValidationResult<A>,
    vrB: ValidationResult<B>
) = ValidationResult.tupledN(NonEmptyList.semigroup(), vrA, vrB)

fun <A, B, C> validateN(
    vrA: ValidationResult<A>,
    vrB: ValidationResult<B>,
    vrC: ValidationResult<C>
) = ValidationResult.tupledN(NonEmptyList.semigroup(), vrA, vrB, vrC)

