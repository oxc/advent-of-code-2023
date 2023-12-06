import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

data class Race(val duration: Long, val record: Long)

fun main() = day(6) {
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

    part1(check = 288L, { input ->
        val durations = input[0].removePrefix("Time: ").splitToLongs(WHITESPACE)
        val records = input[1].removePrefix("Distance: ").splitToLongs(WHITESPACE)
        durations.zip(records) { duration, record -> Race(duration, record) }
    }) { races ->
        races.productOf {
            it.minMax().span()
        }
    }

    part2(check = 71503L, { input ->
        val duration = input[0].removePrefix("Time: ").replace(WHITESPACE, "").toLong()
        val record = input[1].removePrefix("Distance: ").replace(WHITESPACE, "").toLong()
        Race(duration, record)
    }) { race ->
        race.minMax().span()
    }
}
