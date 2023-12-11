package util.parse

import util.matrix.Matrix

val WHITESPACE = Regex("""\s+""")
fun parseCharMatrix(input: List<String>, padChar: Char = '.') = input.toCharMatrix(padChar)
fun List<String>.toCharMatrix(padChar: Char = '.') = Matrix.fromLines(this, padChar)
fun List<String>.toLongs() = map { it.toLong() }
fun String.splitToLongs() = this.trim().split(' ').toLongs()
fun String.splitToLongs(regex: Regex) = this.trim().split(regex).toLongs()
fun List<String>.toInts() = map { it.toInt() }
fun String.splitToInts() = this.trim().split(' ').toInts()