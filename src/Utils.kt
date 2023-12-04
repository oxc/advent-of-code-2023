import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun <T : Any?> T.println(): T {
    println(this)
    return this
}

fun <T : AbstractMatrix<*>> T.println(printThis: Boolean = false): T {
    if (printThis) println(this)
    println(print())
    return this
}

fun List<String>.toCharMatrix(padChar: Char = '.') = Matrix.fromLines(this, padChar)

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
