fun main() = day(4) {
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

    part1(check = 13, { it.map(::parseCard) }) { cards ->
        fun Card.points(): Long {
            val winners = winners()
            return if (winners.isEmpty()) 0 else 2.pow(winners.size - 1)
        }

        cards.sumOf { it.points() }.toIntOrThrow()
    }

    part2(check = 30) { input ->
        val cards = input.mapTo(ArrayList(input.size), ::parseCard).println()
        val counts = cards.mapTo(ArrayList(cards.size)) { 1 }

        for ((i, card) in cards.withIndex()) {
            val count = counts[i]
            val copies = card.winners().size
            for (copi in (i + 1)..(i + copies).coerceAtMost(counts.size - 1)) {
                counts[copi] += count
            }
        }

        counts.sum()
    }
}
