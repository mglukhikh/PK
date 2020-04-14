package javafx

import core.*
import game.Game
import game.GameMove
import game.GameState
import javafx.geometry.Orientation
import javafx.scene.control.Alert
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import tornadofx.*

class MainView : View("Лоскутное королевство") {
    private val colorChoices = (app as MainApp).colorChoices

    private val colors = colorChoices.keys.toList()

    private val playerNumber = colorChoices.size

    private val kingdomSize = if (playerNumber == 2) 7 else 5

    private val game = Game(size = kingdomSize, players = colors.shuffled(), turns = 12).apply {
        nextTurn(GameMove.None)
    }

    private val choiceDepth = game.choiceDepth

    private val kingdomPanes = mutableMapOf<PlayerColor, KingdomPane>()

    private val currentChoicePanes = mutableListOf<ChoicePane>()

    private val nextChoicePanes = mutableListOf<ChoicePane>()

    private lateinit var turnPane: StackPane

    private val orientationPanes = mutableListOf<StackPane>()

    private var currentDirection: Direction = Direction.TO_RIGHT

    private var currentDominoIndex = 0

    private lateinit var currentDominoToPlace: Domino

    private lateinit var currentPointToPlace: Point

    private var currentFirstDominoPane: StackPane? = null

    private var currentSecondDominoPane: StackPane? = null

    private val scorePanes = mutableMapOf<PlayerColor, StackPane>()

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
                    choicePanes()
                }
            }
            // Зона статуса
            bottom {
                vbox {
                    thickSeparator()
                    hbox {
                        statusPanes()
                    }
                }
            }
            showNextDominos()
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

    private class KingdomPane(val cells: Map<Point, StackPane>)

    private fun VBox.kingdomPane(color: PlayerColor): KingdomPane {
        val limit = kingdomSize - 1
        val cells = mutableMapOf<Point, StackPane>()
        gridpane {
            for (y in -limit..limit) {
                row {
                    for (x in -limit..limit) {
                        stackpane {
                            val point = Point(x, y)
                            cells[point] = this
                            emptyRectangle()
                            if (x == 0 && y == 0) {
                                kingCircle()
                                showKing(color)
                            } else {
                                text()
                                setOnMouseMoved {
                                    currentFirstDominoPane?.showSquare()
                                    currentSecondDominoPane?.showSquare()
                                    currentPointToPlace = point
                                    showDominoToPlaceIfApplicable(color)
                                }
                                setOnMousePressed {
                                    currentPointToPlace = point
                                    placeDomino(color)
                                }
                            }
                        }
                    }
                }
            }
        }
        val result = KingdomPane(cells)
        kingdomPanes[color] = result
        return result
    }

    private fun showDominoToPlaceIfApplicable(color: PlayerColor) {
        val state = game.state
        if (color != game.colorToMove || state !is GameState.PlaceCurrentDomino) {
            return
        }
        val domino = DirectedDomino(currentDominoToPlace, currentDirection)
        val kingdom = kingdom(color)
        if (!kingdom.isDominoApplicable(currentPointToPlace, domino)) {
            return
        }
        val firstPoint = currentPointToPlace
        val secondPoint = currentPointToPlace + currentDirection
        val pane = kingdomPanes.getValue(color)
        currentFirstDominoPane = pane.cells.getValue(firstPoint).apply {
            showSquare(currentDominoToPlace.first)
        }
        currentSecondDominoPane = pane.cells.getValue(secondPoint).apply {
            showSquare(currentDominoToPlace.second)
        }
    }

    private fun placeDomino(color: PlayerColor) {
        val state = game.state
        if (color != game.colorToMove || state !is GameState.PlaceCurrentDomino) {
            return
        }
        if (!game.nextTurn(GameMove.PlaceCurrentDomino(currentPointToPlace, currentDirection))) {
            return
        }
        val firstPoint = currentPointToPlace
        val secondPoint = currentPointToPlace + currentDirection
        val pane = kingdomPanes.getValue(color)
        val firstDominoPane = pane.cells.getValue(firstPoint)
        val secondDominoPane = pane.cells.getValue(secondPoint)
        if (currentFirstDominoPane != firstDominoPane && currentFirstDominoPane != secondDominoPane) {
            currentFirstDominoPane?.showSquare()
        }
        currentFirstDominoPane = null
        if (currentSecondDominoPane != firstDominoPane && currentSecondDominoPane != secondDominoPane) {
            currentSecondDominoPane?.showSquare()
        }
        currentSecondDominoPane = null
        firstDominoPane.showSquare(currentDominoToPlace.first)
        secondDominoPane.showSquare(currentDominoToPlace.second)
        showCurrentTurn()
        showScore(color)
        when (game.state) {
            is GameState.PlaceCurrentDomino -> {
                currentDominoIndex = (currentDominoIndex + 1) % choiceDepth
                showCurrentDominos()
                showNextDominos()
                currentDominoToPlace = game.currentDominos[currentDominoIndex]
                showOrientationPane()
            }
            is GameState.MapNextDomino -> {
                currentDominoIndex = (currentDominoIndex + 1) % choiceDepth
                clearOrientationPane()
            }
            else -> {

            }
        }
    }

    // =======================================================================

    private class ChoicePane(val choice: StackPane, val left: StackPane, val right: StackPane)

    private fun VBox.choicePanes() {
        for (i in 0 until choiceDepth) {
            choicePane(i).apply {
                currentChoicePanes += this
            }
            separator()
        }
        thickSeparator()
        for (i in 0 until choiceDepth) {
            choicePane(i, handleClicks = true).apply {
                nextChoicePanes += this
            }
            separator()
        }
    }

    private fun VBox.choicePane(index: Int, handleClicks: Boolean = false): ChoicePane {
        lateinit var result: ChoicePane
        hbox {
            val choice = stackpane {
                emptyRectangle()
                kingCircle()
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
            result = ChoicePane(choice, left, right)
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
        nextChoicePanes[nextIndex].choice.showKing(state.color)
        showCurrentTurn()
        if (game.state is GameState.PlaceCurrentDomino) {
            showCurrentDominos()
            showNextDominos()
            currentDominoToPlace = game.currentDominos[currentDominoIndex]
            showOrientationPane()
        }
    }

    private fun showCurrentDominos() {
        showDominosForChoice(game.currentDominos, game.currentDominoMapping, currentChoicePanes)
    }

    private fun showNextDominos() {
        showDominosForChoice(game.nextDominos, game.nextDominoMapping, nextChoicePanes)
    }

    private fun showDominosForChoice(dominos: List<Domino>, mapping: Map<Int, PlayerColor>, panes: List<ChoicePane>) {
        dominos.forEachIndexed { index, domino ->
            val kingColor = mapping[index]
            panes[index].choice.showKing(kingColor)
            panes[index].left.showSquare(domino.first)
            panes[index].right.showSquare(domino.second)
        }
    }

    // =======================================================================

    private fun HBox.statusPanes() {
        stackpane {
            turnPane = this
            emptyRectangle()
            kingCircle()
            showKing(game.colorToMove)
        }
        thickSeparator()
        gridpane {
            row {
                stackpane {
                    orientationPanes += this
                    emptyRectangle()
                    text()
                    setOnMousePressed {
                        changeOrientation(Direction.TO_UP)
                    }
                }
                stackpane {
                    orientationPanes += this
                    emptyRectangle()
                    text()
                    setOnMousePressed {
                        changeOrientation(Direction.TO_RIGHT)
                    }
                }
            }
            row {
                stackpane {
                    orientationPanes += this
                    emptyRectangle()
                    text()
                    setOnMousePressed {
                        changeOrientation(Direction.TO_LEFT)
                    }
                }
                stackpane {
                    orientationPanes += this
                    emptyRectangle()
                    text()
                    setOnMousePressed {
                        changeOrientation(Direction.TO_DOWN)
                    }
                }
            }
        }
        thickSeparator()
        for (color in colors) {
            vbox {
                stackpane {
                    emptyRectangle()
                    kingCircle()
                    showKing(color)
                }
                stackpane {
                    scorePanes[color] = this
                    emptyRectangle()
                    text(game.score(color).toString())
                }
            }
            separator()
        }
        thickSeparator()
        button("Пропустить ход!").setOnAction {
            if (!game.nextTurn(GameMove.None)) {
                alert(Alert.AlertType.WARNING, "Сейчас пропустить ход нельзя!")
            } else {
                showCurrentTurn()
                currentDominoIndex = (currentDominoIndex + 1) % choiceDepth
                clearOrientationPane()
            }
        }
    }

    private fun showCurrentTurn() {
        val color = game.colorToMove ?: return
        (turnPane.children[1] as Circle).apply {
            fill = color.toGraphicColor()
        }
    }

    private fun showScore(color: PlayerColor) {
        val scorePane = scorePanes.getValue(color)
        (scorePane.children[1] as Text).apply {
            text = game.score(color).toString()
        }
    }

    private fun showOrientationPane() {
        currentFirstDominoPane?.showSquare()
        currentSecondDominoPane?.showSquare()
        currentFirstDominoPane = null
        currentSecondDominoPane = null
        val state = game.state
        if (state !is GameState.PlaceCurrentDomino) {
            return
        }
        val mapping = orientationPaneMapping.getValue(currentDirection)
        orientationPanes.forEachIndexed { index, pane ->
            when (index) {
                mapping.first -> pane.showSquare(currentDominoToPlace.first)
                mapping.second -> pane.showSquare(currentDominoToPlace.second)
                else -> pane.showSquare()
            }
        }
    }

    private fun clearOrientationPane() {
        orientationPanes.forEach { pane ->
            pane.showSquare()
        }
    }

    private fun changeOrientation(direction: Direction) {
        val state = game.state
        if (state !is GameState.PlaceCurrentDomino) {
            return
        }
        currentDirection = direction
        showOrientationPane()
    }

    // =======================================================================

    private fun StackPane.kingCircle(): Circle {
        return circle(radius = cellSize / 3) {
            fill = Color.LIGHTGRAY
        }
    }

    private fun StackPane.showSquare(square: Square? = null) {
        (children[0] as Rectangle).apply {
            fill = square?.terrain?.toGraphicColor() ?: Color.LIGHTGRAY
        }
        (children[1] as Text).apply {
            text = if (square != null && square.crowns > 0) {
                "${square.crowns}"
            } else {
                ""
            }
        }
    }

    private fun StackPane.showKing(color: PlayerColor?) {
        (children[1] as Circle).apply {
            fill = color?.toGraphicColor() ?: Color.LIGHTGRAY
        }
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

    private fun Terrain.toGraphicColor(): Color {
        return when (this) {
            Terrain.CENTER -> Color.LIGHTGRAY
            Terrain.PLAIN -> Color.LEMONCHIFFON
            Terrain.FOREST -> Color.DARKGREEN
            Terrain.WATER -> Color.SKYBLUE
            Terrain.GRASS -> Color.LIGHTGREEN
            Terrain.SWAMP -> Color.DARKGOLDENROD
            Terrain.MINE -> Color.BROWN
        }
    }

    companion object {
        private const val cellSize = 40.0

        private val orientationPaneMapping = mutableMapOf(
            Direction.TO_RIGHT to (0 to 1),
            Direction.TO_LEFT to (3 to 2),
            Direction.TO_DOWN to (1 to 3),
            Direction.TO_UP to (2 to 0)
        )
    }
}