package experiment

fun useSomething() {
    val result = something {
        println(abc)
        println("test")
        42
    }
    println(result)
}

@Suppress("ClassName")
object something {
    const val abc = "abc"

    inline fun eager(crossinline c: () -> Int): Int {
        println("eager")
        return c() + 1
    }

    inline operator fun invoke(crossinline c: something.() -> Int): Int {
        println("object")
        val i = this.c() + 1
        return i
    }
}
