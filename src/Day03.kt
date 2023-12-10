import util.matrix.CharField
import util.matrix.CharLine
import util.matrix.asString

fun main() = day(3) {
    fun CharLine.isPartNumber() = grow(1).asSequence().any { it.value != '.' && !it.value.isDigit() }
    fun CharLine.toInt() = asString().toInt()

    part1(check = 4361, ::parseCharMatrix) { matrix ->
        val numbers = matrix.scanAllLines { it.isDigit() }.filter { it.isPartNumber() }.toList()
        numbers.sumOf { it.toInt() }
    }

    part2(check = 467835, ::parseCharMatrix) { matrix ->
        fun CharLine.stars() = grow(1).asSequence().filter { it.value == '*' }.toList()

        val numbers = matrix.scanAllLines { it.isDigit() }

        val gearCandidates = mutableMapOf<CharField, MutableList<Int>>()
        numbers.forEach { number ->
            number.stars().forEach { star ->
                gearCandidates.getOrPut(star) { mutableListOf() } += number.toInt()
            }
        }

        val gears = gearCandidates.filterValues { it.size == 2 }

        gears.values.sumOf { (a, b) -> a * b }
    }
}
