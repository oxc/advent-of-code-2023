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

fun lcm(a: Long, b: Long): Long {
    val larger = if (a > b) a else b
    val maxLcm = a * b
    var lcm = larger
    while (lcm <= maxLcm) {
        if (lcm % a == 0L && lcm % b == 0L) {
            return lcm
        }
        lcm += larger
    }
    return maxLcm
}

fun gcd(a: Long, b: Long): Long {
    var num1 = a
    var num2 = b
    while (num2 != 0L) {
        val temp = num2
        num2 = num1 % num2
        num1 = temp
    }
    return num1
}

fun main() {
    check(3 absmod 4 == 3)
    check(-3 absmod 4 == 1)
    check(-7 absmod 4 == 1)
}