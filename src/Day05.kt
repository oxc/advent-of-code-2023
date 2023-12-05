import java.util.*

typealias Category = String

data class Range(val source: LongRange, val offset: Long) {
    operator fun contains(value: Long): Boolean {
        return value in source
    }

    operator fun get(value: Long): Long? {
        if (value !in source) return null
        return value + offset
    }
}

data class Mapper(val from: Category, val to: Category, val ranges: List<Range>) : (Long) -> Long {
    override fun invoke(value: Long): Long {
        for (range in ranges) {
            return range[value] ?: continue
        }
        return value
    }
}

data class Puzzle(val seeds: Set<Long>, val mappers: Map<Category, Mapper>) {
    companion object {
        fun parse(input: List<String>): Puzzle {
            val lines = LinkedList(input)
            val seeds = lines.pop().split(':').last().trim().split(' ').toLongSet()

            val mappers = mutableMapOf<Category, Mapper>()
            do {
                check(lines.pop().isEmpty())
                val (from, to) = lines.pop().removeSuffix(" map:").split("-to-")
                check(from.isNotBlank())
                check(to.isNotBlank())
                val ranges = mutableListOf<Range>()
                while (lines.isNotEmpty() && lines.first().isNotBlank()) {
                    val (destStart, sourceStart, rangeLen) = lines.pop().split(' ').toLongs()
                    ranges += Range(sourceStart..<(sourceStart + rangeLen), destStart - sourceStart)
                }
                mappers[from] = Mapper(from, to, ranges)
            } while (lines.isNotEmpty())

            return Puzzle(seeds, mappers)
        }
    }
}

fun main() {
    fun part1(input: List<String>): Long {
        val puzzle = Puzzle.parse(input).println()

        fun Puzzle.findLocation(seed: Long): Long {
            var category = "seed"
            var number = seed
            print("seed $number: ")
            do {
                val mapper = puzzle.mappers[category]!!
                number = mapper(number)
                category = mapper.to
                print("$category $number, ")
            } while (category != "location")
            println("")
            return number
        }

        return puzzle.seeds.minOf { puzzle.findLocation(it) }
    }

    fun part2(input: List<String>): Long {
        return input.size.toLong()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day05_test")
    check(part1(testInput).println() == 35L)

    val input = readInput("Day05")
    part1(input).println()
    part2(input).println()
}
