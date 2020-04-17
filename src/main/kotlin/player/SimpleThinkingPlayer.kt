package player

import core.DirectedDomino
import core.Direction
import core.Domino
import core.Point
import game.GameMove
import game.GameState

class SimpleThinkingPlayer : AbstractPlayer() {
    private fun bestMoveAndScore(currentDomino: Domino): Pair<Int, GameMove> {
        var bestScore = kingdom.score() - 1
        var bestMove: GameMove = GameMove.None
        for (direction in Direction.values()) {
            val directedDomino = DirectedDomino(currentDomino, direction)
            for (row in -game.size..game.size) {
                for (column in -game.size..game.size) {
                    val point = Point(row, column)
                    if (!kingdom.addDomino(point, directedDomino)) {
                        continue
                    }
                    val score = kingdom.score()
                    kingdom.removeLastDomino()
                    if (score > bestScore) {
                        bestScore = score
                        bestMove = GameMove.PlaceCurrentDomino(point, direction)
                    }
                }
            }
        }
        return bestScore to bestMove
    }

    override fun nextMove(): GameMove {
        assert(game.state.color == color)
        return when (game.state) {
            is GameState.MapNextDomino -> {
                val chosen = game.nextDomino.withIndex().maxBy { (index, domino) ->
                    if (index in game.nextDominoMapping) -1
                    else bestMoveAndScore(domino).first
                }?.index ?: throw AssertionError("No domino to choose")
                GameMove.MapNextDomino(chosen)
            }
            is GameState.PlaceCurrentDomino -> {
                val currentDomino = game.currentDominoToPlace
                bestMoveAndScore(currentDomino).second
            }
            is GameState.Start -> GameMove.None
            is GameState.End -> GameMove.None
        }
    }
}