package javafx

import core.PlayerColor
import javafx.fxml.FXML
import javafx.scene.control.ComboBox

class StartDialogController {

    @FXML
    private lateinit var yellow: ComboBox<String>

    @FXML
    private lateinit var red: ComboBox<String>

    @FXML
    private lateinit var green: ComboBox<String>

    @FXML
    private lateinit var blue: ComboBox<String>

    @FXML
    fun initialize() {
        listOf(yellow, red, green, blue).forEach { box ->
            box.items.addAll(comboContent.toTypedArray())
            box.selectionModel.select(0)
        }
    }

    private fun Int.toPlayerChoice(): PlayerChoice = PlayerChoice.values()[this]

    fun getPlayerChoice(color: PlayerColor): PlayerChoice {
        val box = when (color) {
            PlayerColor.YELLOW -> yellow
            PlayerColor.RED -> red
            PlayerColor.GREEN -> green
            PlayerColor.BLUE -> blue
        }
        return box.selectionModel.selectedIndex.toPlayerChoice()
    }

    companion object {
        private val comboContent = PlayerChoice.values().map { it.str }
    }
}