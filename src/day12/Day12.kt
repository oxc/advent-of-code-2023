package day12

import day
import kotlinx.coroutines.runBlocking
import println
import util.collection.repeat
import util.coroutines.pmap
import util.parse.splitToInts
import wtf
import java.util.concurrent.ConcurrentHashMap

sealed class Spring(val c: Char) {
    override fun toString() = c.toString()
}

object Unknown : Spring('?')
sealed class KnownSpring(c: Char) : Spring(c)
object Operational : KnownSpring('.')
object Damaged : KnownSpring('#')

data class Record(val springs: List<Spring>, val blocks: List<Int>)

data class CalculationStep(
    val next: Spring?,
    val blockRemaining: Int?,
    val ixSpring: Int,
    val ixBlock: Int,
)

val debug = false
inline fun debug(msg: () -> String) {
    if (debug) println(msg())
}

fun main() = day(12) {
    fun parseRecords(lines: List<String>, repeat: Int = 1) = lines.map { line ->
        val (layout, groups) = line.split(' ')
        val springs = layout.map {
            when (it) {
                '.' -> Operational
                '#' -> Damaged
                '?' -> Unknown
                else -> wtf("Unknown spring: $it")
            }
        }.repeat(repeat, separator = Unknown).toList()
        val numbers = groups.splitToInts(',').repeat(repeat).toList()
        Record(springs, numbers)
    }

    class Calculation(record: Record) {

        private val springs = record.springs
        private val blocks = record.blocks

        private val cache = ConcurrentHashMap<CalculationStep, Long>()

        fun countArrangementsMemoized(
            step: CalculationStep,
            collectedSprings: String
        ): Long = cache.getOrPut(step) {
            countArrangements(step, collectedSprings)
        }

        fun countArrangements(
            step: CalculationStep,
            collectedSprings: String,
        ): Long {
            val (next, blockRemaining, ixSpring, ixBlock) = step

            fun recurse(
                nextSpring: Spring? = springs.getOrNull(ixSpring),
                block: Int? = blockRemaining,
                advanceSpring: Int = 1,
                advanceBlock: Int = 0,
            ) = countArrangementsMemoized(
                CalculationStep(
                    nextSpring,
                    block,
                    ixSpring + advanceSpring,
                    ixBlock + advanceBlock,
                ),
                if (debug && advanceSpring == 0) collectedSprings else collectedSprings + next?.c
            )

            fun fail(msg: String): Long {
                debug { "Error: $collectedSprings, next=$next, block=$blockRemaining" }
                debug { msg }
                return 0
            }

            return when {
                blockRemaining != null -> when (blockRemaining) {
                    // 0 block, next one must be operational
                    0 -> when (next) {
                        Unknown -> recurse(Operational, advanceSpring = 0)
                        null, Operational -> {
                            recurse(block = null)
                        }

                        Damaged -> fail("Block does not end")
                    }

                    else -> when (next) {
                        Unknown -> recurse(Damaged, advanceSpring = 0)
                        Damaged -> recurse(block = blockRemaining - 1)

                        null, Operational -> fail("Block is not finished")
                    }
                }

                else -> when (next) {
                    Operational -> recurse()

                    Damaged -> {
                        if (ixBlock >= blocks.size) fail("No further block")
                        else recurse(block = blocks[ixBlock] - 1, advanceBlock = 1)
                    }

                    Unknown -> {
                        val operational = recurse(Operational, advanceSpring = 0)
                        val damaged = recurse(Damaged, advanceSpring = 0)
                        operational + damaged
                    }

                    null -> if (ixBlock >= blocks.size) {
                        1
                    } else fail("Not all blocks filled")
                }
            }
        }
    }

    fun Record.countArrangements(): Long {
        val calculation = Calculation(this)
        return calculation.countArrangements(CalculationStep(springs.first(), null, 1, 0), "").also {
            println("\n$it arrangements for $this")
        }
    }
    part1(check = 21, ::parseRecords) { records ->
        records.map { it.countArrangements() }.println().sum()
    }


    part2(check = 525152L, { parseRecords(it, 5) }) { records ->
        runBlocking {
            records.withIndex().pmap { (ix, record) ->
                println("\n\n[${ix + 1}/${records.size}] Counting possible arrangements for $record\n")
                record.countArrangements()
            }.println().sum()
        }
    }
}