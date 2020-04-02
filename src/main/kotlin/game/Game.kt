package game

import core.*

class Game(val size: Int, val players: List<PlayerColor>, val turns: Int = (size * size - 1) / 2) {

    private val deck = Deck()

    var state: GameState = GameState.Start

    val turn: Int get() = state.turn

    var nextPatches = deck.take(players.size)
        private set

    val nextPatchMapping = mutableMapOf<Int, PlayerColor>()

    var currentPatches = emptyList<Patch>()
        private set

    var currentPatchMapping = mutableMapOf<Int, PlayerColor>()
        private set

    private val kingdoms = players.associateWith { Kingdom(size) }

    fun mapNextPatch(player: PlayerColor, index: Int): Boolean {
        if (index !in nextPatches.indices) {
            throw AssertionError("Incorrect patch index: $index")
        }
        return nextPatchMapping.put(index, player) == null
    }

    fun takeCurrentPatch(index: Int): Pair<PlayerColor, Patch>? {
        val color = currentPatchMapping[index] ?: return null
        currentPatchMapping.remove(index)
        val patch = currentPatches[index]
        return color to patch
    }

    fun transferPatchesToCurrent(regenerateNext: Boolean) {
        currentPatches = nextPatches
        currentPatchMapping = nextPatchMapping.toMutableMap()
        nextPatches = if (regenerateNext) deck.take(players.size) else emptyList()
        nextPatchMapping.clear()
    }

    fun kingdom(player: PlayerColor) = kingdoms.getValue(player)

    fun addPatch(player: PlayerColor, point: Point, patch: DirectedPatch): Boolean {
        return kingdoms.getValue(player).addPatch(point, patch)
    }

    fun removeLastPatch(player: PlayerColor) {
        kingdoms.getValue(player).removeLastPatch()
    }

    fun scores(): Map<PlayerColor, Int> {
        return kingdoms.mapValues { (_, kingdom) ->
            kingdom.score()
        }
    }

    fun nextTurn(move: GameMove): Boolean {
        when (val state = state) {
            is GameState.Start -> {
                this.state = GameState.MapNextPatch(0, players[0])
            }
            is GameState.End -> {
                return false
            }
            is GameState.MapNextPatch -> {
                if (move !is GameMove.MapNextPatch) {
                    return false
                }
                if (!mapNextPatch(state.color, move.index)) {
                    return false
                }
                if (turn == 0) {
                    val playerIndex = players.indexOf(state.color)
                    if (playerIndex < players.size - 1) {
                        this.state = GameState.MapNextPatch(0, players[playerIndex + 1])
                    } else {
                        transferPatchesToCurrent(regenerateNext = true)
                        this.state = GameState.PlaceCurrentPatch(1, currentPatchMapping.getValue(0))
                    }
                } else {
                    val nextColor = currentPatchMapping.values.firstOrNull()
                    if (nextColor != null) {
                        this.state = GameState.PlaceCurrentPatch(turn, nextColor)
                    } else {
                        if (turn == turns) {
                            this.state = GameState.End
                        } else {
                            transferPatchesToCurrent(regenerateNext = turn < turns - 1)
                            this.state = GameState.PlaceCurrentPatch(turn + 1, currentPatchMapping.getValue(0))
                        }
                    }
                }
            }
            is GameState.PlaceCurrentPatch -> {
                if (move is GameMove.MapNextPatch) {
                    return false
                }
                val index = currentPatchMapping.entries.find {
                    it.value == state.color
                }!!.key
                if (move is GameMove.PlaceCurrentPatch) {
                    if (!addPatch(state.color, move.point, DirectedPatch(currentPatches[index], move.direction))) {
                        return false
                    }
                }
                takeCurrentPatch(index)
                if (turn < turns) {
                    this.state = GameState.MapNextPatch(turn, state.color)
                } else {
                    if (currentPatchMapping.isEmpty()) {
                        this.state = GameState.End
                    } else {
                        this.state = GameState.PlaceCurrentPatch(
                            turn, currentPatchMapping.entries.minBy { (index, _) ->
                                index
                            }!!.value
                        )
                    }
                }
            }
        }
        return false
    }
}