import util.matrix.AbstractMatrix
import util.matrix.Highlight
import util.matrix.Matrix
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines


/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").takeIf { it.exists() }?.readLines()

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

fun <T : AbstractMatrix<*>> T.println(highlight: Highlight = Highlight.NONE): T {
    println(this.print(highlight))
    return this
}

val WHITESPACE = Regex("""\s+""")

fun parseCharMatrix(input: List<String>, padChar: Char = '.') = input.toCharMatrix(padChar)

fun List<String>.toCharMatrix(padChar: Char = '.') = Matrix.fromLines(this, padChar)

fun List<String>.toLongs() = map { it.toLong() }
fun String.splitToLongs() = this.trim().split(' ').toLongs()
fun String.splitToLongs(regex: Regex) = this.trim().split(regex).toLongs()
fun List<String>.toInts() = map { it.toInt() }
fun String.splitToInts() = this.trim().split(' ').toInts()

inline fun <T> Iterable<T>.productOf(selector: (T) -> Long): Long = fold(1L) { acc, i -> acc * selector(i) }
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

fun <T> Sequence<T>.repeatIndefinitely() = sequence { while (true) yieldAll(this@repeatIndefinitely) }
fun <T> Iterable<T>.repeatIndefinitely() = sequence { while (true) yieldAll(this@repeatIndefinitely) }

fun wtf(message: String? = null): Nothing {
    throw Exception("What a terrible failure" + message?.let { ": $it" })
}