import util.matrix.AbstractMatrixElement
import util.matrix.Highlight
import util.matrix.Printer
import java.math.BigInteger
import java.security.MessageDigest

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

fun <T : AbstractMatrixElement<F>, F> T.println(
    printer: Printer<F> = Printer.default(),
    highlight: Highlight<F> = Highlight.none(),
): T {
    println(this.print(printer, highlight))
    return this
}

fun wtf(message: String? = null): Nothing {
    throw Exception("What a terrible failure" + message?.let { ": $it" })
}