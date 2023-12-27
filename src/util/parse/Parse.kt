package util.parse

import util.matrix.Matrix

val WHITESPACE = Regex("""\s+""")
fun parseCharMatrix(input: List<String>, padChar: Char = '.') = input.toCharMatrix(padChar)
fun List<String>.toCharMatrix(padChar: Char = '.') = Matrix.fromLines(this, padChar)
inline fun List<String>.split(predicate: (String) -> Boolean): List<List<String>> {
    val result = mutableListOf<List<String>>()
    var list = this
    while (list.isNotEmpty()) {
        val chunk = list.takeWhile { !predicate(it) }
        result += chunk
        list = list.drop(chunk.size + 1)
    }
    return result
}

fun List<String>.toLongs() = map { it.trim().toLong() }
fun String.splitToLongs(delimiter: Char = ' ') = this.trim().split(delimiter).toLongs()
fun String.splitToLongs(regex: Regex) = this.trim().split(regex).toLongs()
fun List<String>.toInts() = map { it.trim().toInt() }
fun String.splitToInts(delimiter: Char = ' ') = this.trim().split(delimiter).toInts()