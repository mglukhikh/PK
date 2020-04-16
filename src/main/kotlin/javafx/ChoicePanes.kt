package javafx

import core.Domino
import core.PlayerColor
import core.Square
import core.Terrain
import game.Game
import game.GameMove
import game.GameState
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import tornadofx.hbox
import tornadofx.separator
import tornadofx.stackpane
import tornadofx.text

class ChoicePanes(private val game: Game, private val handleNextState: (Boolean) -> Unit) {
    private class Pane(val selector: StackPane, val choice: StackPane, val left: StackPane, val right: StackPane)

    private val currentChoicePanes = mutableListOf<Pane>()

    private val nextChoicePanes = mutableListOf<Pane>()

    fun VBox.create() {
        for (i in 0 until game.choiceDepth) {
            choicePane(i).apply {
                currentChoicePanes += this
            }
            separator()
        }
        thickSeparator()
        for (i in 0 until game.choiceDepth) {
            choicePane(i, handleClicks = true).apply {
                nextChoicePanes += this
            }
            separator()
        }
    }

    private fun VBox.choicePane(index: Int, handleClicks: Boolean = false): Pane {
        lateinit var result: Pane
        hbox {
            val selector = stackpane {
                emptyRectangle()
                selectorArrow()
            }
            val choice = stackpane {
                emptyRectangle()
                kingCrown()
                if (handleClicks) {
                    setOnMousePressed {
                        choiceMade(index)
                    }
                }
            }
            val left = stackpane {
                emptyRectangle()
                text()
            }
            val right = stackpane {
                emptyRectangle()
                text()
            }
            thickSeparator()
            result = Pane(selector, choice, left, right)
        }
        return result
    }

    private fun choiceMade(nextIndex: Int) {
        val state = game.state
        if (state !is GameState.MapNextDomino) {
            return
        }
        if (!game.nextTurn(GameMove.MapNextDomino(nextIndex))) {
            return
        }
        showNextDomino()
        handleNextState(false)
    }

    fun showCurrentDomino() {
        showDominoForChoice(game.currentDomino, game.currentDominoMapping, currentChoicePanes)
    }

    fun showNextDomino() {
        showDominoForChoice(game.nextDomino, game.nextDominoMapping, nextChoicePanes)
    }

    private fun showDominoForChoice(dominoList: List<Domino>, mapping: Map<Int, PlayerColor>, panes: List<Pane>) {
        if (dominoList.isEmpty()) {
            for (index in 0 until game.choiceDepth) {
                panes[index].selector.showArrow(null)
                panes[index].choice.showKing(null)
                panes[index].left.showSquare(Square(Terrain.CENTER))
                panes[index].right.showSquare(Square(Terrain.CENTER))
            }
            return
        }
        dominoList.forEachIndexed { index, domino ->
            val kingColor = mapping[index]
            if (panes == nextChoicePanes && game.state is GameState.MapNextDomino && kingColor == null) {
                panes[index].selector.showArrow(game.colorToMove)
            } else {
                panes[index].selector.showArrow(null)
            }
            panes[index].choice.showKing(kingColor)
            panes[index].left.showSquare(domino.first)
            panes[index].right.showSquare(domino.second)
        }
    }
}