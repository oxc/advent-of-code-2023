package day12

import day
import println
import util.parse.splitToInts
import wtf
import java.util.*

sealed class Spring(val c: Char) {
    override fun toString() = c.toString()
}

object Unknown : Spring('?')
sealed class KnownSpring(c: Char) : Spring(c)
object Operational : KnownSpring('.')
object Damaged : KnownSpring('#')

data class Record(val springs: List<Spring>, val blocks: List<Int>)

fun main() = day(12) {
    fun parseRecords(lines: List<String>) = lines.map { line ->
        val (layout, groups) = line.split(' ')
        val springs = layout.map {
            when (it) {
                '.' -> Operational
                '#' -> Damaged
                '?' -> Unknown
                else -> wtf("Unknown spring: $it")
            }
        }
        val numbers = groups.splitToInts(',')
        Record(springs, numbers)
    }

    part1(check = 21, ::parseRecords) { records ->

        fun countArrangements(
            next: Spring?,
            blockRemaining: Int?,
            springs: LinkedList<Spring>,
            blocks: LinkedList<Int>,
            collectedSprings: LinkedList<Spring>,
        ): Int {
            when (next) {
                Operational, Damaged -> collectedSprings.add(next)
                else -> {}
            }
            fun debug(msg: String) {
                if (false) println(msg)
            }
            debug("${collectedSprings.joinToString("")} ${springs.joinToString("")}, next=$next, blockRemaining=$blockRemaining, blocks=$blocks")

            fun recurse(
                nextSpring: Spring? = springs.removeFirstOrNull(),
                block: Int? = blockRemaining,
                _springs: LinkedList<Spring> = springs,
                _blocks: LinkedList<Int> = blocks,
                _collectedSprings: LinkedList<Spring> = collectedSprings,
            ) = countArrangements(nextSpring, block, _springs, _blocks, _collectedSprings)

            fun fail(msg: String): Int {
                debug(msg)
                return 0
            }

            return when {
                blockRemaining != null -> when (blockRemaining) {
                    // 0 block, next one must be operational
                    0 -> when (next) {
                        Unknown -> recurse(Operational)
                        null, Operational -> {
                            blocks.pop()
                            recurse(block = null)
                        }

                        Damaged -> fail("Block does not end")
                    }

                    else -> when (next) {
                        Unknown -> recurse(Damaged)
                        Damaged -> recurse(block = blockRemaining - 1)

                        null, Operational -> fail("Block is not finished")
                    }
                }

                else -> when (next) {
                    Operational -> recurse()

                    Damaged -> {
                        if (blocks.isEmpty()) fail("No further block")
                        else recurse(block = blocks.first - 1)
                    }

                    Unknown -> {
                        val operational = recurse(
                            Operational,
                            null,
                            LinkedList(springs),
                            LinkedList(blocks),
                            LinkedList(collectedSprings)
                        )
                        val damaged = recurse(
                            Damaged,
                        )
                        operational + damaged
                    }

                    null -> if (blocks.isEmpty()) {
                        println("Possible arrangement: ${collectedSprings.joinToString("")}")
                        1
                    } else fail("Not all blocks filled")
                }
            }
        }

        fun Record.countArrangements(): Int {
            println("\n\nCounting possible arrangements for $this\n")
            val springs = LinkedList(springs)
            return countArrangements(
                springs.removeFirst(),
                null,
                springs,
                LinkedList(blocks),
                LinkedList()
            ).also {
                println("\n$it arrangements for $this")
            }
        }

        records.println().sumOf { it.countArrangements() }
    }
}