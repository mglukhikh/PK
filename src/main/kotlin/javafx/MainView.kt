package javafx

import core.PlayerColor
import core.Point
import game.Game
import javafx.geometry.Orientation
import javafx.scene.layout.*
import javafx.scene.paint.Color
import tornadofx.*

class MainView : View("Лоскутное королевство") {
    private val colorChoices = (app as MainApp).colorChoices

    private val colors = colorChoices.keys.toList()

    private val playerNumber = colorChoices.size

    private val kingdomSize = if (playerNumber == 2) 7 else 5

    private val kingdomPanes = mutableMapOf<PlayerColor, KingdomPane>()

    private val game = Game(size = kingdomSize, players = colors)

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
            }
        }
    }

    private class KingdomPane(val grid: GridPane, val cells: Map<Point, StackPane>)

    private fun VBox.kingdomPane(color: PlayerColor): KingdomPane {
        val limit = kingdomSize - 1
        val cells = mutableMapOf<Point, StackPane>()
        val grid = gridpane {
            for (y in -limit..limit) {
                row {
                    for (x in -limit..limit) {
                        stackpane {
                            cells[Point(x, y)] = this
                            rectangle(width = cellSize, height = cellSize) {
                                stroke = Color.BLACK
                                fill = Color.LIGHTGRAY
                            }
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