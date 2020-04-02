package player

import game.GameMove
import game.GameState

class BrainDeadPlayer : AbstractPlayer() {
    override fun nextMove(): GameMove {
        assert(game.state.color == color)
        return when (game.state) {
            is GameState.MapNextPatch -> {
                val chosen = game.nextPatches.indices.find { index ->
                    index !in game.nextPatchMapping
                } ?: throw AssertionError("No patches to choose")
                GameMove.MapNextPatch(chosen)
            }
            else -> GameMove.None
        }
    }
}