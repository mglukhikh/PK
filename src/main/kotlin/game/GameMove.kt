package game

import core.Direction
import core.Point

sealed class GameMove {
    object None : GameMove()

    class MapNextPatch(val index: Int) : GameMove()

    class PlaceCurrentPatch(val point: Point, val direction: Direction) : GameMove()
}