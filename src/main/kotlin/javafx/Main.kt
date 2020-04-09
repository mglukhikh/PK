package javafx

import core.PlayerColor
import javafx.application.Application
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import tornadofx.App
import tornadofx.View
import tornadofx.alert

class MainView : View("Лоскутное королевство") {
    override val root = BorderPane()
}

class MainApp : App(MainView::class) {

    override fun start(stage: Stage) {
        //Sets up dialog before main application.
        val dialog = StartDialog()
        //Retrieves response value.
        val result = dialog.showAndWait()
        if (result.isPresent && result.get().buttonData == ButtonBar.ButtonData.OK_DONE) {
            val choices = PlayerColor.values().map { dialog.getPlayerChoice(it) }
            if (choices.count { it != PlayerChoice.NONE } < 2) {
                alert(Alert.AlertType.ERROR, "Выбрано слишком мало игроков!")
            } else {
                val playersDescription = choices.withIndex().filter { (_, choice) ->
                    choice != PlayerChoice.NONE
                }.joinToString { (index, choice) ->
                    PlayerColor.values()[index].str + " " + choice.str
                }
                alert(Alert.AlertType.INFORMATION, "Играют", playersDescription)
                super.start(stage)
            }
        } else {
            alert(Alert.AlertType.ERROR, "Игра не начата!")
        }
    }
}

fun main(args: Array<String>) {
    Application.launch(MainApp::class.java, *args)
}