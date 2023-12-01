fun main() {
    fun part1(input: List<String>): Int {
        return input
            .map { line ->
                (line.find { it.isDigit() }!!.digitToInt() * 10
                + line.findLast { it.isDigit() }!!.digitToInt())  }
            .fold(0) { s, v -> s + v }
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 142)

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}
