package game

import core.Direction
import core.Point

sealed class GameMove {
    object None : GameMove()

    class MapNextDomino(val index: Int) : GameMove()

    class PlaceCurrentDomino(val point: Point, val direction: Direction) : GameMove()
}