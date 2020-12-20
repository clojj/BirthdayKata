package experiment

import arrow.core.*
import arrow.core.computations.either
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.fx.coroutines.parTraverseEither
import com.clojj.birthdaykata.domain.ValidationResult
import kotlinx.coroutines.*
import java.io.IOException

fun main() {

    val scope = CoroutineScope(SupervisorJob())

    scope.launch {
        program()
    }

    Thread.sleep(1000)
}

private suspend fun program() {
    val results = either<Nel<String>, List<Int>> {
        // TODO validate many fields
        val validated = validateComposite("abc")()

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

