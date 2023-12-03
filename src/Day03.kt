fun main() {
    fun CharLine.isPartNumber() = grow(1).asSequence().any { it != '.' && !it.isDigit() }

    fun part1(input: List<String>): Int {
        val matrix = input.toCharMatrix().println()
        val numbers = matrix.scanAllLines { it.isDigit() }.filter { it.isPartNumber() }.toList()
        return numbers.sumOf { it.asString().toInt() }
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 4361)

    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}
