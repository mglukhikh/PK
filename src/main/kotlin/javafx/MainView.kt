package javafx

import core.*
import game.Game
import game.GameMove
import game.GameState
import javafx.geometry.Orientation
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

    private var currentPatchIndex = 0

    private lateinit var currentPatchToPlace: Patch

    private lateinit var currentPointToPlace: Point

    private var currentFirstPatchPane: StackPane? = null

    private var currentSecondPatchPane: StackPane? = null

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
            showNextPatches()
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
                                    currentFirstPatchPane?.showSquare()
                                    currentSecondPatchPane?.showSquare()
                                    currentPointToPlace = point
                                    showPatchToPlaceIfApplicable(color)
                                }
                                setOnMousePressed {
                                    currentPointToPlace = point
                                    placePatch(color)
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

    private fun showPatchToPlaceIfApplicable(color: PlayerColor) {
        val state = game.state
        if (color != game.colorToMove || state !is GameState.PlaceCurrentPatch) {
            return
        }
        val directedPatch = DirectedPatch(currentPatchToPlace, currentDirection)
        val kingdom = kingdom(color)
        if (!kingdom.isPatchApplicable(currentPointToPlace, directedPatch)) {
            return
        }
        val firstPoint = currentPointToPlace
        val secondPoint = currentPointToPlace + currentDirection
        val pane = kingdomPanes.getValue(color)
        currentFirstPatchPane = pane.cells.getValue(firstPoint).apply {
            showSquare(currentPatchToPlace.first)
        }
        currentSecondPatchPane = pane.cells.getValue(secondPoint).apply {
            showSquare(currentPatchToPlace.second)
        }
    }

    private fun placePatch(color: PlayerColor) {
        val state = game.state
        if (color != game.colorToMove || state !is GameState.PlaceCurrentPatch) {
            return
        }
        if (!game.nextTurn(GameMove.PlaceCurrentPatch(currentPointToPlace, currentDirection))) {
            return
        }
        val firstPoint = currentPointToPlace
        val secondPoint = currentPointToPlace + currentDirection
        val pane = kingdomPanes.getValue(color)
        val firstPatchPane = pane.cells.getValue(firstPoint)
        val secondPatchPane = pane.cells.getValue(secondPoint)
        if (currentFirstPatchPane != firstPatchPane && currentFirstPatchPane != secondPatchPane) {
            currentFirstPatchPane?.showSquare()
        }
        currentFirstPatchPane = null
        if (currentSecondPatchPane != firstPatchPane && currentSecondPatchPane != secondPatchPane) {
            currentSecondPatchPane?.showSquare()
        }
        currentSecondPatchPane = null
        firstPatchPane.showSquare(currentPatchToPlace.first)
        secondPatchPane.showSquare(currentPatchToPlace.second)
        showCurrentTurn()
        showScore(color)
        when (game.state) {
            is GameState.PlaceCurrentPatch -> {
                currentPatchIndex = (currentPatchIndex + 1) % choiceDepth
                showCurrentPatches()
                showNextPatches()
                currentPatchToPlace = game.currentPatches[currentPatchIndex]
                showOrientationPane()
            }
            is GameState.MapNextPatch -> {
                currentPatchIndex = (currentPatchIndex + 1) % choiceDepth
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
        if (state !is GameState.MapNextPatch) {
            return
        }
        if (!game.nextTurn(GameMove.MapNextPatch(nextIndex))) {
            return
        }
        nextChoicePanes[nextIndex].choice.showKing(state.color)
        showCurrentTurn()
        if (game.state is GameState.PlaceCurrentPatch) {
            println("Current patch index: $currentPatchIndex")
            showCurrentPatches()
            showNextPatches()
            currentPatchToPlace = game.currentPatches[currentPatchIndex]
            showOrientationPane()
        }
    }

    private fun showCurrentPatches() {
        showPatchesForChoice(game.currentPatches, game.currentPatchMapping, currentChoicePanes)
    }

    private fun showNextPatches() {
        showPatchesForChoice(game.nextPatches, game.nextPatchMapping, nextChoicePanes)
    }

    private fun showPatchesForChoice(patches: List<Patch>, mapping: Map<Int, PlayerColor>, panes: List<ChoicePane>) {
        patches.forEachIndexed { index, patch ->
            val kingColor = mapping[index]
            panes[index].choice.showKing(kingColor)
            panes[index].left.showSquare(patch.first)
            panes[index].right.showSquare(patch.second)
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
        currentFirstPatchPane?.showSquare()
        currentSecondPatchPane?.showSquare()
        currentFirstPatchPane = null
        currentSecondPatchPane = null
        val state = game.state
        if (state !is GameState.PlaceCurrentPatch) {
            return
        }
        val mapping = orientationPaneMapping.getValue(currentDirection)
        orientationPanes.forEachIndexed { index, pane ->
            when (index) {
                mapping.first -> pane.showSquare(currentPatchToPlace.first)
                mapping.second -> pane.showSquare(currentPatchToPlace.second)
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
        if (state !is GameState.PlaceCurrentPatch) {
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
            Terrain.SWAMP -> Color.BROWN
            Terrain.MINE -> Color.BEIGE
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