fun main() {
    fun part1(input: List<String>): Int {
        return input
            .map { line ->
                (line.find { it.isDigit() }!!.digitToInt() * 10
                + line.findLast { it.isDigit() }!!.digitToInt())  }
            .fold(0) { s, v -> s + v }
    }

    fun part2(input: List<String>): Int {
        val namedDigits = listOf(
            "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"
        )
        val reDigits = Regex("""${namedDigits.joinToString("|")}|\d""")
        fun extractDigits(line: String): List<Int> {
            val result = mutableListOf<Int>()
            var offset = 0
            do {
                val match = reDigits.find(line, offset) ?: break
                if (match.value.length == 1) {
                    result.add(match.value[0].digitToInt())
                } else {
                    result.add(namedDigits.indexOf(match.value) + 1)
                }
                offset = match.range.first + 1
            } while (true)

            return result
        }

        return input
            .map { line -> extractDigits(line) }
            .map { digits -> digits.first() * 10 + digits.last() }
            .fold(0) { s, v -> s + v }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 142)

    val testInput2 = readInput("Day01_test2")
    check(part2(testInput2) == 281)

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}
