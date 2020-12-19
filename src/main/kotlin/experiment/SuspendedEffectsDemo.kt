package experiment

import arrow.Kind
import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.concurrent
import arrow.fx.extensions.io.concurrent.dispatchers
import arrow.fx.extensions.io.unsafeRun.runBlocking
import arrow.fx.fix
import arrow.fx.typeclasses.Concurrent
import arrow.unsafe

interface HasSideEffectHandling<F> : Concurrent<F> {
    suspend fun <A> Kind<F, A>.asSuspended(): A
    suspend fun <A> myAttempt(f: suspend () -> A): Either<Throwable, A>
}

// @Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
object IOSideEffectHandling : HasSideEffectHandling<ForIO>, Concurrent<ForIO> by IO.concurrent(dispatchers()) {

    override suspend fun <A> Kind<ForIO, A>.asSuspended(): A {
        return this.fix().suspended()
    }

    override suspend fun <A> myAttempt(f: suspend () -> A): Either<Throwable, A> {
        return effect(f).attempt().asSuspended()
    }
}

// ----------------------------------------------------------

interface HasConsole<ENV> {
    suspend fun ENV.printToConsole(s: String)
    suspend fun ENV.readFromConsole(): String?
}

interface LiveConsole<F, ENV> : HasConsole<ENV> where ENV : HasSideEffectHandling<F> {

    override suspend fun ENV.printToConsole(s: String) {
        println(s)
    }

    override suspend fun ENV.readFromConsole(): String? {
        return myAttempt { readLine() }
            .fold({ "" }, { it })
    }
}

// -------------------------------------------------------------

interface Env : HasConsole<Env>, HasSideEffectHandling<ForIO>

val env: Env = object : Env, HasConsole<Env> by object : LiveConsole<ForIO, Env> {}, HasSideEffectHandling<ForIO> by IOSideEffectHandling {}

private suspend fun Env.program(): String? {
    printToConsole("Hello")
    return readFromConsole()
}

fun main() {
    val x = unsafe {
        runBlocking {
            env.effect { env.program() }
        }
    }

    println(x)
}
