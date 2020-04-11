package javafx

import core.PlayerColor
import javafx.application.Application
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.stage.Stage
import tornadofx.App
import tornadofx.alert

class MainApp : App(MainView::class) {

    val colorChoices = mutableMapOf<PlayerColor, PlayerChoice>()

    override fun start(stage: Stage) {
        //Sets up dialog before main application.
        val dialog = StartDialog()
        //Retrieves response value.
        val result = dialog.showAndWait()
        if (result.isPresent && result.get().buttonData == ButtonBar.ButtonData.OK_DONE) {
            val choices = PlayerColor.values().map { dialog.getPlayerChoice(it) }
            val playerCount = choices.count { it != PlayerChoice.NONE }
            when {
                playerCount < 2 -> {
                    alert(Alert.AlertType.ERROR, "Выбрано слишком мало игроков!")
                }
                playerCount == 2 -> {
                    alert(Alert.AlertType.ERROR, "Игра с двумя игроками пока не поддерживается!")
                }
                else -> {
                    val indexedPlayerChoices = choices.withIndex().filter { (_, choice) ->
                        choice != PlayerChoice.NONE
                    }
                    indexedPlayerChoices.associateTo(colorChoices) { (index, choice) ->
                        PlayerColor.values()[index] to choice
                    }
                    val playersDescription = indexedPlayerChoices.joinToString { (index, choice) ->
                        PlayerColor.values()[index].str + " " + choice.str
                    }
                    alert(Alert.AlertType.INFORMATION, "Играют", playersDescription)
                    super.start(stage)
                }
            }
        } else {
            alert(Alert.AlertType.ERROR, "Игра не начата!")
        }
    }
}

fun main(args: Array<String>) {
    Application.launch(MainApp::class.java, *args)
}