package javafx

import core.PlayerColor
import javafx.fxml.FXMLLoader
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog

class StartDialog : Dialog<ButtonType>() {

    private var controller: StartDialogController

    init {
        title = "Настройки игры"
        with(dialogPane) {
            buttonTypes.add(ButtonType("Начать", ButtonBar.ButtonData.OK_DONE))
            buttonTypes.add(ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE))
            val loader = FXMLLoader()
            loader.location = MainApp::class.java.getResource("/javafx/start.fxml")
            content = loader.load()
            controller = loader.getController()
        }
    }

    fun getPlayerChoice(color: PlayerColor): PlayerChoice {
        return controller.getPlayerChoice(color)
    }
}