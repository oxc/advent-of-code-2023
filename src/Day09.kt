typealias History = List<Long>

fun main() = day(9) {
    fun parseHistory(input: List<String>) = input.map { it.splitToLongs() }

    part1(check = 114L, ::parseHistory) { input ->
        fun History.findDifferences() = zipWithNext().map { (a, b) -> b - a }

        fun History.predictNextValue(): Long {
            if (all { it == 0L }) return 0
            val nextLine = findDifferences()
            return last() + nextLine.predictNextValue()
        }

        input.sumOf { it.predictNextValue() }
    }
}
