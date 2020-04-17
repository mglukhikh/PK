package game

import core.PlayerColor
import player.AbstractPlayer

class GameRunner(size: Int, private val playerNumber: Int) {
    private val colors = PlayerColor.values().take(playerNumber).shuffled(kotlin.random.Random)

    private val game = Game(size, colors, turns = 12)

    val players = mutableMapOf<PlayerColor, AbstractPlayer>()

    fun addPlayer(player: AbstractPlayer) {
        val color = colors.first { it !in players }
        player.color = color
        player.game = game
        player.kingdom = game.kingdom(color)
        players[color] = player
    }

    fun play(): Map<PlayerColor, Int> {
        assert(players.size == playerNumber)
        while (game.state != GameState.End) {
            val move = when (val state = game.state) {
                is GameState.Start, is GameState.End -> GameMove.None
                is GameState.MapNextDomino -> players[state.color]!!.nextMove()
                is GameState.PlaceCurrentDomino -> players[state.color]!!.nextMove()
            }
            game.nextTurn(move)
        }
        return game.scores()
    }
}