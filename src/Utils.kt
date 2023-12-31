import util.matrix.*
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

fun <T : AbstractMatrix3DElement<F>, F> T.println(
    printer: Printer3D<F> = Printer3D.default(),
    highlight: Highlight3D<F> = Highlight3D.none(),
): T {
    println(this.print(printer, highlight))
    return this
}

fun <T : SparseMatrix<F>, F> T.println(
    printer: Printer<SparseFieldData<F>> = Printer.default(),
    highlight: Highlight<SparseFieldData<F>> = Highlight.none(),
): T {
    matrix.println(printer, highlight)
    return this
}

fun wtf(message: String? = null): Nothing {
    throw Exception("What a terrible failure" + message?.let { ": $it" })
}