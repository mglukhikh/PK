package javafx

import core.PlayerColor
import core.Square
import core.Terrain
import javafx.geometry.Orientation
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.Text
import tornadofx.polygon
import tornadofx.rectangle
import tornadofx.separator

private const val cellSize = 40.0

fun StackPane.kingCrown(): Shape {
    return polygon(
        cellSize / 4, 7 * cellSize / 8,
        cellSize / 8, cellSize / 8,
        3 * cellSize / 8, cellSize / 2,
        cellSize / 2, cellSize / 8,
        5 * cellSize / 8, cellSize / 2,
        7 * cellSize / 8, cellSize / 8,
        3 * cellSize / 4, 7 * cellSize / 8
    ) {
        fill = Color.LIGHTGRAY
    }
}

fun StackPane.showSquare(square: Square? = null) {
    (children[0] as Shape).apply {
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

fun StackPane.showKing(color: PlayerColor?) {
    (children[1] as Shape).apply {
        fill = color?.toGraphicColor() ?: Color.LIGHTGRAY
    }
}

fun StackPane.emptyRectangle(): Rectangle {
    return rectangle(width = cellSize, height = cellSize) {
        stroke = Color.BLACK
        fill = Color.LIGHTGRAY
    }
}

fun PlayerColor.toGraphicColor(): Color {
    return when (this) {
        PlayerColor.YELLOW -> Color.YELLOW
        PlayerColor.RED -> Color.RED
        PlayerColor.GREEN -> Color.GREEN
        PlayerColor.BLUE -> Color.BLUE
    }
}

fun Terrain.toGraphicColor(): Color {
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

fun HBox.thickSeparator() = separator(Orientation.VERTICAL) {
    minWidth = cellSize * 2
}

fun VBox.thickSeparator() = separator(Orientation.HORIZONTAL) {
    minHeight = cellSize * 2
}

