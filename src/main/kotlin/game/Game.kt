package game

import core.*

class Game(val size: Int, val players: List<PlayerColor>, val turns: Int = (size * size - 1) / 2) {

    private val deck = Deck()

    var state: GameState = GameState.Start

    val turn: Int get() = state.turn

    val colorToMove: PlayerColor? get() = state.color

    val choiceDepth = if (players.size == 2) 4 else players.size

    var nextDomino = deck.take(choiceDepth)
        private set

    val nextDominoMapping = mutableMapOf<Int, PlayerColor>()

    var currentDomino = emptyList<Domino>()
        private set

    var currentDominoMapping = mutableMapOf<Int, PlayerColor>()
        private set

    val currentDominoToPlace: Domino
        get() = currentDomino[currentDominoIndex]

    val currentDominoIndex: Int
        get() = currentDominoMapping.entries.find {
            it.value == state.color
        }!!.key

    private val kingdoms = players.associateWith { Kingdom(size) }

    fun mapNextDomino(player: PlayerColor, index: Int): Boolean {
        if (index !in nextDomino.indices) {
            throw AssertionError("Incorrect domino index: $index")
        }
        return nextDominoMapping.put(index, player) == null
    }

    fun takeCurrentDomino(color: PlayerColor): Pair<PlayerColor, Domino>? {
        val index = currentDominoMapping.entries.find {
            it.value == color
        }!!.key
        return takeCurrentDomino(index)
    }

    fun takeCurrentDomino(index: Int): Pair<PlayerColor, Domino>? {
        val color = currentDominoMapping[index] ?: return null
        currentDominoMapping.remove(index)
        val domino = currentDomino[index]
        return color to domino
    }

    fun transferDominoToCurrent(regenerateNext: Boolean) {
        currentDomino = nextDomino
        currentDominoMapping = nextDominoMapping.toMutableMap()
        nextDomino = if (regenerateNext) deck.take(choiceDepth) else emptyList()
        nextDominoMapping.clear()
    }

    fun kingdom(player: PlayerColor) = kingdoms.getValue(player)

    fun addDomino(player: PlayerColor, point: Point, domino: DirectedDomino): Boolean {
        return kingdoms.getValue(player).addDomino(point, domino)
    }

    fun removeLastDomino(player: PlayerColor) {
        kingdoms.getValue(player).removeLastDomino()
    }

    fun scores(): Map<PlayerColor, Int> {
        return kingdoms.mapValues { (_, kingdom) ->
            kingdom.score()
        }
    }

    fun score(color: PlayerColor): Int {
        return kingdom(color).score()
    }

    fun nextTurn(move: GameMove): Boolean {
        when (val state = state) {
            is GameState.Start -> {
                this.state = GameState.MapNextDomino(0, players[0])
            }
            is GameState.End -> {
                return false
            }
            is GameState.MapNextDomino -> {
                if (move !is GameMove.MapNextDomino) {
                    return false
                }
                if (!mapNextDomino(state.color, move.index)) {
                    return false
                }
                if (turn == 0) {
                    val playerIndex = players.indexOf(state.color)
                    if (playerIndex < players.size - 1) {
                        this.state = GameState.MapNextDomino(0, players[playerIndex + 1])
                    } else {
                        transferDominoToCurrent(regenerateNext = true)
                        this.state = GameState.PlaceCurrentDomino(1, currentDominoMapping.getValue(0))
                    }
                } else {
                    val nextColor = currentDominoMapping.entries.minByOrNull { (index, _) -> index }?.value
                    if (nextColor != null) {
                        this.state = GameState.PlaceCurrentDomino(turn, nextColor)
                    } else {
                        if (turn == turns) {
                            this.state = GameState.End
                        } else {
                            transferDominoToCurrent(regenerateNext = turn < turns - 1)
                            this.state = GameState.PlaceCurrentDomino(turn + 1, currentDominoMapping.getValue(0))
                        }
                    }
                }
            }
            is GameState.PlaceCurrentDomino -> {
                if (move is GameMove.MapNextDomino) {
                    return false
                }
                val currentDominoToPlace = currentDominoToPlace
                if (move is GameMove.PlaceCurrentDomino) {
                    if (!addDomino(state.color, move.point, DirectedDomino(currentDominoToPlace, move.direction))) {
                        return false
                    }
                }
                takeCurrentDomino(state.color)
                if (turn < turns) {
                    this.state = GameState.MapNextDomino(turn, state.color)
                } else {
                    if (currentDominoMapping.isEmpty()) {
                        this.state = GameState.End
                    } else {
                        this.state = GameState.PlaceCurrentDomino(
                            turn, currentDominoMapping.entries.minByOrNull { (index, _) ->
                                index
                            }!!.value
                        )
                    }
                }
            }
        }
        return true
    }
}