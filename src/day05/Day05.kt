package day05

import day
import util.parse.splitToLongs
import util.parse.toLongs
import java.util.*

typealias Category = String

data class Range(val source: LongRange, val offset: Long) {
    val dest = (source.first + offset)..(source.last + offset)

    operator fun contains(value: Long): Boolean {
        return value in source
    }

    fun map(value: Long): Long? {
        if (value !in source) return null
        return value + offset
    }
}

data class Mapper(val from: Category, val to: Category, val ranges: MutableList<Range>) {
    init {
        ranges.sortBy { it.source.first }
    }

    fun map(value: LongRange): Sequence<LongRange> = sequence {
        if (value.isEmpty()) return@sequence
        for (range in ranges) {
            // comes before this range, yield unaltered
            if (value.last < range.source.first) {
                yield(value)
                return@sequence
            }
            // is exactly this range
            if (value == range.source) {
                yield(range.dest)
                return@sequence
            }
            // is fully contained in this range
            if (value.first >= range.source.first && value.last <= range.source.last) {
                yield((value.first + range.offset)..(value.last + range.offset))
                return@sequence
            }
            // overlaps at least on the left side with this range, split in two at range start and recurse
            if (value.first < range.source.first && value.last >= range.source.first) {
                yield(value.first..<range.source.first)
                yieldAll(map(range.source.first..value.last))
                return@sequence
            }
            // overlaps on the right side with this range, split in two and recurse
            if (value.first < range.source.last) {
                yield((value.first + range.offset)..range.dest.last)
                yieldAll(map(range.source.last + 1..value.last))
                return@sequence
            }
            // is after this range, let next range handle this
        }
        yield(value)
    }
}

data class Puzzle(val seedRanges: List<LongRange>, val mappers: Map<Category, Mapper>) {
    private fun findMinimalLocation(category: Category, range: LongRange): Long {
        if (category == "location") return range.first
        val mapper = mappers[category] ?: throw NoSuchElementException("No mapper for $category")
        return mapper.map(range).minOf { findMinimalLocation(mapper.to, it) }
    }

    fun findMinimalLocation(seed: LongRange) = findMinimalLocation("seed", seed)

    fun findMinimalLocation() = seedRanges.minOf { findMinimalLocation(it) }
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

fun main() = day(5) {
    part1(check = 35L, { input ->
        val lines = LinkedList(input)
        val seedRanges = lines.pop().split(':').last().splitToLongs().map { it..it }
        val mappers = parseMappers(lines)
        Puzzle(seedRanges, mappers)
    }) { puzzle ->
        puzzle.findMinimalLocation()
    }

    part2(46L, { input ->
        val lines = LinkedList(input)
        val numbers = LinkedList(lines.pop().split(':').last().splitToLongs())
        val seedRanges = LinkedList<LongRange>()
        while (numbers.isNotEmpty()) {
            val seed = numbers.pop()
            val count = numbers.pop()
            seedRanges += seed..<(seed + count)
        }

        val mappers = parseMappers(lines)
        Puzzle(seedRanges, mappers)
    }) { puzzle ->
        puzzle.findMinimalLocation()
    }
}
