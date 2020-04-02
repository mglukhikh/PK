package player

import core.Kingdom
import core.PlayerColor
import game.Game
import game.GameMove

abstract class AbstractPlayer {
    lateinit var kingdom: Kingdom
    lateinit var color: PlayerColor
    lateinit var game: Game
    abstract fun nextMove(): GameMove
}