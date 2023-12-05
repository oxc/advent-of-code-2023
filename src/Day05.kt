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

data class Puzzle(val seedSources: List<Iterable<Long>>, val mappers: Map<Category, Mapper>) {
    fun seeds(): Sequence<Long> {
        return sequence {
            seedSources.forEach { yieldAll(it) }
        }
    }

    private fun findLocation(category: Category, number: Long): Long {
        if (category == "location") return number
        val mapper = mappers[category] ?: throw NoSuchElementException("No mapper for $category")
        return findLocation(mapper.to, mapper(number))
    }

    fun findLocation(seed: Long) = findLocation("seed", seed)
}

fun parseMappers(lines: LinkedList<String>): MutableMap<Category, Mapper> {
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
    return mappers
}

fun main() = runParallel {
    fun part1(input: List<String>): Long {
        fun parse(input: List<String>): Puzzle {
            val lines = LinkedList(input)
            val seeds = lines.pop().split(':').last().splitToLongs().toSet()
            val mappers = parseMappers(lines)
            return Puzzle(listOf(seeds), mappers)
        }

        val puzzle = parse(input).println()

        return puzzle.seeds().minOf { puzzle.findLocation(it) }
    }

    suspend fun part2(input: List<String>): Long {
        fun parse(input: List<String>): Puzzle {
            val lines = LinkedList(input)
            val numbers = LinkedList(lines.pop().split(':').last().splitToLongs())
            val seedRanges = LinkedList<LongRange>()
            while (numbers.isNotEmpty()) {
                val seed = numbers.pop()
                val count = numbers.pop()
                seedRanges += seed..<(seed + count)
            }

            val mappers = parseMappers(lines)
            return Puzzle(seedRanges, mappers)
        }

        val puzzle = parse(input).println()

        return puzzle.seedSources.pmap { it.minOf { puzzle.findLocation(it) } }.min()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day05_test")
    check(part1(testInput).println() == 35L)
    check(part2(testInput).println() == 46L)

    val input = readInput("Day05")
    part1(input).println()
    part2(input).println()
}
