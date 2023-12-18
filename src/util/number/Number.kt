package util.number

fun IntRange.span() = last - first + 1
fun LongRange.span() = last - first + 1
fun Int.pow(exp: Int): Long {
    var result = 1L
    for (i in 1..exp) {
        result *= this
    }
    return result
}

fun Long.toIntOrThrow(): Int {
    return if (this >= Int.MIN_VALUE && this <= Int.MAX_VALUE) {
        toInt()
    } else {
        throw ArithmeticException("Value too large to fit in an Int")
    }
}

infix fun Int.absmod(mod: Int): Int {
    return (this % mod).let { if (it < 0) it + mod else it }
}

fun main() {
    check(3 absmod 4 == 3)
    check(-3 absmod 4 == 1)
    check(-7 absmod 4 == 1)
}