@JvmInline
value class Card(val label: Char) {
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

data class Hand(val cards: List<Card>, val bid: Long) {
    override fun toString() = "${cards.joinToString("")} $bid"
}

data class TypedHand(val hand: Hand, val type: Type) {
    override fun toString() = "$hand $type"
}

interface GameRules {
    fun Card.value(): Int
    fun Hand.type(): Type
}

fun main() = day(7) {
    fun parseHands(input: List<String>) = input.map { line ->
        val (sHand, sBid) = line.split(' ')
        val cards = sHand.map { Card(it) }
        Hand(cards, sBid.toLong())
    }

    fun GameRules.winnings(hands: List<Hand>): Long {
        val withType = hands.map { TypedHand(it, it.type()) }
        val compareByType = compareBy<TypedHand> { it.type.value }
        val compareByValue = (0..<5).fold(compareByType) { acc, i -> acc.thenBy { it.hand.cards[i].value() } }
        val ranked = withType.sortedWith(compareByValue)
        return ranked.mapIndexed { index, hand -> hand.hand.bid * (index + 1) }.sum()
    }

    fun typeFromTopCounts(count: Int, count2: Int): Type {
        return when (count) {
            5 -> Type.FiveOfAKind
            4 -> Type.FourOfAKind
            3 -> {
                if (count2 == 2) Type.FullHouse else Type.ThreeOfAKind
            }

            2 -> {
                if (count2 == 2) Type.TwoPair else Type.OnePair
            }

            else -> Type.HighCard
        }
    }

    part1(check = 6440L, ::parseHands) { hands ->
        val rules = object : GameRules {
            override fun Card.value() = when (label) {
                'A' -> 14
                'K' -> 13
                'Q' -> 12
                'J' -> 11
                'T' -> 10
                else -> label.digitToInt()
            }

            override fun Hand.type(): Type {
                val sortedCounts = cards.groupingBy { it.label }.eachCount()
                    .toList().sortedByDescending { it.second }
                val count = sortedCounts.first().second
                val count2 = sortedCounts.getOrNull(1)?.second ?: 0
                return typeFromTopCounts(count, count2)
            }
        }

        rules.winnings(hands)
    }

    part2(check = 5905L, ::parseHands) { hands ->
        val rules = object : GameRules {
            override fun Card.value() = when (label) {
                'A' -> 14
                'K' -> 13
                'Q' -> 12
                'J' -> 1
                'T' -> 10
                else -> label.digitToInt()
            }

            override fun Hand.type(): Type {
                val counts = cards.groupingBy { it.label }.eachCountTo(mutableMapOf())
                val jokers = counts.remove('J') ?: 0
                val sortedCounts = counts.toList().sortedByDescending { it.second }
                val count = sortedCounts.firstOrNull()?.second ?: 0
                val count2 = sortedCounts.getOrNull(1)?.second ?: 0
                return typeFromTopCounts(count + jokers, count2)
            }
        }

        rules.winnings(hands)
    }
}
