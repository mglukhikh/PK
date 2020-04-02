package game

import core.PlayerColor

sealed class GameState(val turn: Int) {
    open val color: PlayerColor? get() = null

    object Start : GameState(0)

    class MapNextPatch(turn: Int, override val color: PlayerColor) : GameState(turn)

    class PlaceCurrentPatch(turn: Int, override val color: PlayerColor) : GameState(turn)

    object End : GameState(Int.MAX_VALUE)
}