enum class Color {
    red, green, blue
}

typealias Dice = Map<Color, Int>

fun main() {
    data class Game(val id: Int, val sets: List<Dice>) {
        fun isPossibleWith(dice: Dice) = sets.all { set ->
            set.all { (color, count) ->
                count <= dice.getOrDefault(color, 0)
            }
        }
    }

    fun parseGame(line: String): Game {
        val (sgame, ssets) = line.split(':')
        val gameId = sgame.removePrefix("Game ").toInt()
        val sets = ssets.split(";").map { sset ->
            sset.trim().split(",").map { sdice ->
                val (scount, scolor) = sdice.trim().split(' ')
                val count = scount.toInt()
                val color = Color.valueOf(scolor)
                color to count
            }.toMap()
        }
        return Game(gameId, sets)
    }

    fun part1(input: List<String>): Int {
        val games = input.map(::parseGame).println()
        val bag = mapOf(Color.red to 12, Color.green to 13, Color.blue to 14)
        return games.filter {
            it.isPossibleWith(bag)
        }.println().sumOf { it.id }
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part1(testInput).println() == 8)

    val input = readInput("Day02")
    part1(input).println()
    part2(input).println()
}
