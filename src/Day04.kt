fun main() {
    data class Card(val id: Int, val winningNumbers: Set<Number>, val numbers: Set<Number>)

    fun parseCard(line: String): Card {
        val (sCard, sNumbers) = line.split(':')
        val gameId = sCard.removePrefix("Card").trim().toInt()
        val (winningNumbers, numbers) = sNumbers.split("|", limit = 2).map {
            it.trim().split("""\s+""".toRegex()).map { sNumber ->
                sNumber.trim().toInt()
            }.toSet()
        }
        return Card(gameId, winningNumbers, numbers)
    }

    fun Card.winners() = winningNumbers.intersect(numbers)

    fun part1(input: List<String>): Int {
        val cards = input.map(::parseCard).println()

        fun Card.points(): Long {
            val winners = winners()
            return if (winners.isEmpty()) 0 else 2.pow(winners.size - 1)
        }

        return cards.sumOf { it.points() }.toIntOrThrow()
    }

    fun part2(input: List<String>): Int {
        val cards = input.mapTo(ArrayList(input.size), ::parseCard).println()
        val counts = cards.mapTo(ArrayList(cards.size)) { 1 }

        for ((i, card) in cards.withIndex()) {
            val count = counts[i]
            val copies = card.winners().size
            for (copi in (i + 1)..(i + copies).coerceAtMost(counts.size - 1)) {
                counts[copi] += count
            }
        }

        return counts.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput).println() == 13)
    check(part2(testInput).println() == 30)

    val input = readInput("Day04")
    part1(input).println()
    part2(input).println()
}
