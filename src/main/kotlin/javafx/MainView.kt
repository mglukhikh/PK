package javafx

import core.*
import game.Game
import game.GameMove
import game.GameState
import javafx.scene.control.Alert
import javafx.scene.layout.*
import javafx.scene.text.Text
import player.SimpleThinkingPlayer
import tornadofx.*
import kotlin.concurrent.timer

class MainView : View("Лоскутное королевство") {
    private val colorChoices = (app as MainApp).colorChoices

    private val colors = colorChoices.keys.toList()

    private val playerNumber = colorChoices.size

    private val kingdomSize = if (playerNumber == 2) 7 else 5

    private val game = Game(size = kingdomSize, players = colors.shuffled(), turns = 12).apply {
        nextTurn(GameMove.None)
    }

    private val aiPlayers = colorChoices.entries.filter { it.value == PlayerChoice.AI }.associate { (color, _) ->
        color to SimpleThinkingPlayer().apply {
            this.color = color
            this.game = this@MainView.game
            kingdom = game.kingdom(color)
        }
    }

    private val choiceDepth = game.choiceDepth

    private val kingdomPanes = mutableMapOf<PlayerColor, KingdomPane>()

    private lateinit var turnPane: StackPane

    private val orientationPanes = mutableListOf<StackPane>()

    private var currentDirection: Direction = Direction.TO_RIGHT

    private var currentDominoIndex = 0

    private var currentDominoToPlace: Domino? = null

    private lateinit var currentPointToPlace: Point

    private var currentFirstDominoPane: StackPane? = null

    private var currentSecondDominoPane: StackPane? = null

    private val scorePanes = mutableMapOf<PlayerColor, StackPane>()

    private fun kingdom(player: PlayerColor) = game.kingdom(player)

    override val root = BorderPane()

    private val choicePanes = ChoicePanes(game) { handleNextGameState(it) }

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
                    with(choicePanes) {
                        create()
                    }
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
            choicePanes.showNextDomino()
        }
        subscribe<AutoTurnEvent> {
            makeComputerTurn()
        }
        startTimerIfNeeded()
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
                                kingCrown()
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
        val currentDominoToPlace = currentDominoToPlace ?: return
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
        val currentDominoToPlace = currentDominoToPlace ?: return
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
        showScore(color)
        handleNextGameState()
    }

    private fun showKingdom(color: PlayerColor) {
        val limit = kingdomSize - 1
        val pane = kingdomPanes.getValue(color)
        for (y in -limit..limit) {
            for (x in -limit..limit) {
                if (x == 0 && y == 0) continue
                val point = Point(x, y)
                val square = kingdom(color).getSquare(point)
                pane.cells.getValue(point).showSquare(square)
            }
        }
    }

    private fun handleNextGameState(changeCurrentDomino: Boolean = true) {
        showCurrentTurn()
        when (game.state) {
            is GameState.PlaceCurrentDomino -> {
                if (changeCurrentDomino) {
                    currentDominoIndex = game.currentDominoIndex
                }
                choicePanes.showCurrentDomino()
                choicePanes.showNextDomino()
                currentDominoToPlace = game.currentDomino[currentDominoIndex]
                showOrientationPane()
            }
            is GameState.MapNextDomino -> {
                if (changeCurrentDomino) {
                    currentDominoIndex = (currentDominoIndex + 1) % choiceDepth
                }
                choicePanes.showNextDomino()
                clearOrientationPane()
            }
            is GameState.End -> {
                clearOrientationPane()
                gameOverAlert()
            }
            is GameState.Start -> {
                throw AssertionError("Should not be here")
            }
        }
    }

    private fun gameOverAlert() {
        alert(Alert.AlertType.CONFIRMATION, "Игра окончена!",
            "Очков набрано: ${game.scores().entries.joinToString { (color, score) ->
                "${color.str}: $score"
            }}")
    }

    // =======================================================================

    private fun HBox.statusPanes() {
        stackpane {
            turnPane = this
            emptyRectangle()
            kingCrown()
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
                        changeOrientation(Direction.TO_DOWN)
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
                        changeOrientation(Direction.TO_UP)
                    }
                }
            }
        }
        thickSeparator()
        for (color in colors) {
            vbox {
                stackpane {
                    emptyRectangle()
                    kingCrown()
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
                if (game.state is GameState.MapNextDomino) {
                    alert(
                        Alert.AlertType.WARNING,
                        header = "Сейчас пропустить ход нельзя",
                        content = "Выберите следующий лоскут напротив стрелок"
                    )
                } else if (game.state is GameState.End) {
                    alert(
                        Alert.AlertType.WARNING,
                        header = "Сейчас пропустить ход нельзя",
                        content = "Игра уже окончена"
                    )
                }
            } else {
                handleNextGameState()
            }
        }
    }

    private fun showCurrentTurn() {
        val color = game.colorToMove ?: return
        turnPane.showKing(color)
    }

    private fun showScore(color: PlayerColor) {
        val scorePane = scorePanes.getValue(color)
        (scorePane.children[1] as Text).apply {
            text = game.score(color).toString()
        }
    }

    private fun showOrientationPane() {
        val currentDominoToPlace = currentDominoToPlace ?: return
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

    private var pauseCounter = 0

    private class AutoTurnEvent : FXEvent()

    private fun makeComputerTurn() {
        val color = game.colorToMove
        val aiPlayer = aiPlayers[color]
        if (color != null && aiPlayer != null) {
            pauseCounter++
            if (pauseCounter == 10) {
                pauseCounter = 0
                val move = aiPlayer.nextMove()
                game.nextTurn(move)
                showKingdom(color)
                showScore(color)
                handleNextGameState()
            }
        } else {
            pauseCounter = 0
        }
    }

    private fun startTimerIfNeeded() {
        if (aiPlayers.isNotEmpty()) {
            timer(daemon = true, period = 100) {
                if (game.state !is GameState.End) {
                    fire(AutoTurnEvent())
                } else {
                    this.cancel()
                }
            }
        }
    }

    // =======================================================================

    companion object {
        private val orientationPaneMapping = mutableMapOf(
            Direction.TO_RIGHT to (0 to 1),
            Direction.TO_LEFT to (3 to 2),
            Direction.TO_UP to (1 to 3),
            Direction.TO_DOWN to (2 to 0)
        )
    }
}