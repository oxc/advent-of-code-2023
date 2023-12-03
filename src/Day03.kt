fun main() {
    fun CharLine.isPartNumber() = grow(1).asSequence().any { it.value != '.' && !it.value.isDigit() }
    fun CharLine.toInt() = asString().toInt()

    fun part1(input: List<String>): Int {
        val matrix = input.toCharMatrix().println()
        val numbers = matrix.scanAllLines { it.isDigit() }.filter { it.isPartNumber() }.toList()
        return numbers.sumOf { it.toInt() }
    }

    fun part2(input: List<String>): Int {
        fun CharLine.stars() = grow(1).asSequence().filter { it.value == '*' }.toList()

        val matrix = input.toCharMatrix()
        val numbers = matrix.scanAllLines { it.isDigit() }

        val gearCandidates = mutableMapOf<CharField, MutableList<Int>>()
        numbers.forEach { number ->
            number.stars().forEach { star ->
                gearCandidates.getOrPut(star) { mutableListOf() } += number.toInt()
            }
        }

        val gears = gearCandidates.filterValues { it.size == 2 }

        return gears.values.sumOf { (a, b) -> a * b }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 4361)
    check(part2(testInput) == 467835)

    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}
