enum class Color {
    red, green, blue
}

typealias Dice = Map<Color, Int>

fun main() = day(2) {
    fun Dice.power() = Color.entries.fold(1) { acc, color ->
        acc * this.getOrDefault(color, 1)
    }

    data class Game(val id: Int, val sets: List<Dice>) {
        fun isPossibleWith(dice: Dice) = sets.all { set ->
            set.all { (color, count) ->
                count <= dice.getOrDefault(color, 0)
            }
        }

        fun minimalSet(): Dice {
            return Color.entries.map { color ->
                color to sets.maxOf { it.getOrDefault(color, 0) }
            }.toMap()
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

    part1(check = 8, { it.map(::parseGame) }) { games ->
        val bag = mapOf(Color.red to 12, Color.green to 13, Color.blue to 14)
        games.filter {
            it.isPossibleWith(bag)
        }.println().sumOf { it.id }
    }

    part2(check = 2286, { it.map(::parseGame) }) { games ->
        games.map { it.minimalSet() }.println().sumOf { it.power() }
    }
}
