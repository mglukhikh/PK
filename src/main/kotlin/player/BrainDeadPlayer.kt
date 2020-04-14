package player

import game.GameMove
import game.GameState

class BrainDeadPlayer : AbstractPlayer() {
    override fun nextMove(): GameMove {
        assert(game.state.color == color)
        return when (game.state) {
            is GameState.MapNextDomino -> {
                val chosen = game.nextDominos.indices.find { index ->
                    index !in game.nextDominoMapping
                } ?: throw AssertionError("No dominos to choose")
                GameMove.MapNextDomino(chosen)
            }
            else -> GameMove.None
        }
    }
}