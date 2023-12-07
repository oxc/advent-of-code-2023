@JvmInline
value class Card(val label: Char) {
    val value
        get() = when (label) {
            'A' -> 14
            'K' -> 13
            'Q' -> 12
            'J' -> 11
            'T' -> 10
            else -> label.digitToInt()
        }

    override fun toString() = label.toString()
}

enum class Type(val value: Int) {
    FiveOfAKind(7),
    FourOfAKind(6),
    FullHouse(5),
    ThreeOfAKind(4),
    TwoPair(3),
    OnePair(2),
    HighCard(1),
}

data class Hand(val cards: List<Card>, val bid: Int)

fun main() = day(7) {
    fun parseHands(input: List<String>) = input.map { line ->
        val (sHand, sBid) = line.println().split(' ')
        val cards = sHand.map { Card(it) }
        Hand(cards, sBid.toInt())
    }

    part1(check = 6440, ::parseHands) { hands ->
        fun Hand.type(): Type {
            val counts = cards.groupingBy { it.label }.eachCount()
            val sortedCounts = counts.toList().sortedByDescending { it.second }
            val (_, count) = sortedCounts.first()
            return when (count) {
                5 -> Type.FiveOfAKind
                4 -> Type.FourOfAKind
                3 -> {
                    val (_, count2) = sortedCounts[1]
                    if (count2 == 2) Type.FullHouse else Type.ThreeOfAKind
                }

                2 -> {
                    val (_, count2) = sortedCounts[1]
                    if (count2 == 2) Type.TwoPair else Type.OnePair
                }

                else -> Type.HighCard
            }
        }

        val compareByType = compareBy<Hand> { it.type().value }
        val compareByValue = (0..<5).fold(compareByType) { acc, i -> acc.thenBy { it.cards[i].value } }
        val ranked = hands.sortedWith(compareByValue)

        ranked.mapIndexed { index, hand -> hand.bid * (index + 1) }.sum()
    }
}
