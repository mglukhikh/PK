package core

import kotlin.random.Random

fun Pair(i: Int) = Pair(i, i)

private val rawDeck = listOf(
    Pair(10),
    Pair(10),
    Pair(20),
    Pair(20),
    Pair(20),
    Pair(20),
    Pair(30),
    Pair(30),
    Pair(30),
    Pair(40),
    Pair(40),
    Pair(50),
    // 12
    Pair(10, 20),
    Pair(10, 30),
    Pair(10, 40),
    Pair(10, 50),
    Pair(20, 30),
    Pair(20, 40),
    // 18
    Pair(11, 20),
    Pair(11, 30),
    Pair(11, 40),
    Pair(11, 50),
    Pair(11, 60),
    // 23
    Pair(21, 10),
    Pair(21, 10),
    Pair(21, 10),
    Pair(21, 10),
    Pair(21, 30),
    Pair(21, 40),
    // 29
    Pair(31, 10),
    Pair(31, 10),
    Pair(31, 20),
    Pair(31, 20),
    Pair(31, 20),
    Pair(31, 20),
    // 35
    Pair(41, 10),
    Pair(41, 30),    
    Pair(51, 10),
    Pair(51, 40),
    Pair(61, 10),
    // 40
    Pair(42, 10),
    Pair(42, 30),    
    Pair(52, 10),
    Pair(52, 40),
    // 44
    Pair(62, 10),
    Pair(62, 50),
    Pair(62, 50),
    Pair(63, 10)
)

internal fun Int.toDeckPatch(direction: Direction): DirectedPatch =
    DirectedPatch(Patch(this + 1, rawDeck[this].first, rawDeck[this].second), direction)

class Deck {
    private var deck = rawDeck.mapIndexed { index, (first, second) ->
        Patch(index + 1, first, second)
    }.shuffled(Random)

    fun take(num: Int): List<Patch> {
        val result = deck.takeLast(num).sortedBy { it.number }
        deck = deck.dropLast(num)
        return result
    }
}
