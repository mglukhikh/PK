package javafx

import javafx.application.Application
import javafx.scene.layout.BorderPane
import tornadofx.App
import tornadofx.View

class MainView : View("Лоскутное королевство") {
    override val root = BorderPane()
}

class MainApp : App(MainView::class)

fun main(args: Array<String>) {
    Application.launch(MainApp::class.java, *args)
}