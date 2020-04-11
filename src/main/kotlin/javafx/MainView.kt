package javafx

import core.PlayerColor
import core.Point
import game.Game
import javafx.geometry.Orientation
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import tornadofx.*

class MainView : View("Лоскутное королевство") {
    private val colorChoices = (app as MainApp).colorChoices

    private val colors = colorChoices.keys.toList()

    private val playerNumber = colorChoices.size

    private val kingdomSize = if (playerNumber == 2) 7 else 5

    private val game = Game(size = kingdomSize, players = colors, turns = 12)

    private val choiceDepth = game.choiceDepth

    private val kingdomPanes = mutableMapOf<PlayerColor, KingdomPane>()

    private val currentChoicePanes = mutableListOf<ChoicePane>()

    private val nextChoicePanes = mutableListOf<ChoicePane>()

    private fun kingdom(player: PlayerColor) = game.kingdom(player)

    override val root = BorderPane()

    init {
        with(root) {
            // Зона полей
            center {
                when (playerNumber) {
                    2 -> kingdomsForTwo()
                    3 -> kingdomsForThree()
                    4 -> kingdomsForFour()
                }
            }
            // Зона выбора
            right {
                vbox {
                    for (i in 0 until choiceDepth) {
                        choicePane().apply {
                            currentChoicePanes += this
                        }
                        separator()
                    }
                    thickSeparator()
                    for (i in 0 until choiceDepth) {
                        choicePane().apply {
                            nextChoicePanes += this
                        }
                        separator()
                    }
                }
            }
        }
    }

    private fun HBox.thickSeparator() = separator(Orientation.VERTICAL) {
        minWidth = cellSize * 2
    }

    private fun VBox.thickSeparator() = separator(Orientation.HORIZONTAL) {
        minHeight = cellSize * 2
    }

    private fun BorderPane.kingdomsForTwo() {
        hbox {
            vbox {
                kingdomPane(colors[0])
            }
            thickSeparator()
            vbox {
                kingdomPane(colors[1])
            }
            thickSeparator()
        }
    }

    private fun BorderPane.kingdomsForThree() {
        hbox {
            vbox {
                kingdomPane(colors[0])
            }
            thickSeparator()
            vbox {
                kingdomPane(colors[1])
            }
            thickSeparator()
            vbox {
                kingdomPane(colors[2])
            }
            thickSeparator()
        }
    }

    private fun BorderPane.kingdomsForFour() {
        vbox {
            hbox {
                vbox {
                    kingdomPane(colors[0])
                }
                thickSeparator()
                vbox {
                    kingdomPane(colors[1])
                }
                thickSeparator()
            }
            thickSeparator()
            hbox {
                vbox {
                    kingdomPane(colors[2])
                }
                thickSeparator()
                vbox {
                    kingdomPane(colors[3])
                }
                thickSeparator()
            }
        }
    }

    private class KingdomPane(val grid: GridPane, val cells: Map<Point, StackPane>)

    private class ChoicePane(val choice: StackPane, val left: StackPane, val right: StackPane)

    private fun VBox.kingdomPane(color: PlayerColor): KingdomPane {
        val limit = kingdomSize - 1
        val cells = mutableMapOf<Point, StackPane>()
        val grid = gridpane {
            for (y in -limit..limit) {
                row {
                    for (x in -limit..limit) {
                        stackpane {
                            cells[Point(x, y)] = this
                            emptyRectangle()
                            if (x == 0 && y == 0) {
                                circle(radius = cellSize / 3) {
                                    fill = color.toGraphicColor()
                                }
                            }
                        }
                    }
                }
            }
        }
        val result = KingdomPane(grid, cells)
        kingdomPanes[color] = result
        return result
    }

    private fun VBox.choicePane(): ChoicePane {
        lateinit var result: ChoicePane
        hbox {
            val choice = stackpane {
                emptyRectangle()
            }
            val left = stackpane {
                emptyRectangle()
            }
            val right = stackpane {
                emptyRectangle()
            }
            thickSeparator()
            result = ChoicePane(choice, left, right)
        }
        return result
    }

    private fun StackPane.emptyRectangle(): Rectangle {
        return rectangle(width = cellSize, height = cellSize) {
            stroke = Color.BLACK
            fill = Color.LIGHTGRAY
        }
    }

    private fun PlayerColor.toGraphicColor(): Color {
        return when (this) {
            PlayerColor.YELLOW -> Color.YELLOW
            PlayerColor.RED -> Color.RED
            PlayerColor.GREEN -> Color.GREEN
            PlayerColor.BLUE -> Color.BLUE
        }
    }

    companion object {
        private const val cellSize = 40.0
    }
}