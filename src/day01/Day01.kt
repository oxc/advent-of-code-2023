package day01

import day

fun main() = day(1) {
    part1(check = 142) { input ->
        input
            .map { line ->
                (line.find { it.isDigit() }!!.digitToInt() * 10
                        + line.findLast { it.isDigit() }!!.digitToInt())
            }
            .fold(0) { s, v -> s + v }
    }

    part2(checks = mapOf("test2" to 281)) { input ->
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

        input
            .map { line -> extractDigits(line) }
            .map { digits -> digits.first() * 10 + digits.last() }
            .fold(0) { s, v -> s + v }
    }
}
