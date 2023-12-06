import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

data class Race(val duration: Long, val record: Long)

fun main() {
    fun Race.minMax(): LongRange {
        // record = (duration - speedup) * speedup
        // speedup_min = -0.5 * (sqrt(duration^2 - 4 * record) - duration)
        // speedup_max = 0.5 * (duration + sqrt(duration^2 - 4 * record))

        val duration = duration.toDouble()
        val record = record.toDouble()
        val a = sqrt(duration.pow(2) - 4 * record)
        val speedupMin = -0.5 * (a - duration)
        val speedupMax = 0.5 * (a + duration)
        println("duration: $duration, record: $record, speedupMin: $speedupMin, speedupMax: $speedupMax")
        return (floor(speedupMin + 1).toLong()..ceil(speedupMax - 1).toLong())
    }

    fun part1(input: List<String>): Long {
        fun parse(input: List<String>): List<Race> {
            val durations = input[0].removePrefix("Time: ").splitToLongs(WHITESPACE)
            val records = input[1].removePrefix("Distance: ").splitToLongs(WHITESPACE)
            return durations.zip(records) { duration, record -> Race(duration, record) }
        }

        val races = parse(input)
        return races.productOf {
            it.minMax().span()
        }
    }

    fun part2(input: List<String>): Long {
        fun parse(input: List<String>): Race {
            val duration = input[0].removePrefix("Time: ").replace(WHITESPACE, "").toLong()
            val record = input[1].removePrefix("Distance: ").replace(WHITESPACE, "").toLong()
            return Race(duration, record)
        }

        val race = parse(input).println()

        return race.minMax().span()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test")
    check(part1(testInput).println() == 288L)
    check(part2(testInput).println() == 71503L)

    val input = readInput("Day06")
    part1(input).println()
    part2(input).println()
}
