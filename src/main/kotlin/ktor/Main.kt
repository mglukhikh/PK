package ktor

import core.*
import core.PlayerColor.*
import game.Game
import game.GameRunner
import game.GameState
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.request.receiveText
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import player.SimpleThinkingPlayer
import kotlin.concurrent.thread

private val runner = GameRunner(5, 3)

private val game: Game
    get() = runner.game

private fun PlayerColor.shortName(): String =
    when (this) {
        YELLOW -> "Alan"
        RED -> "John"
        GREEN -> "Mike"
        BLUE -> "Kate"
    }

private fun TR.showSquare(square: Square?) {
    if (square == null) {
        td { +"-" }
    } else {
        val background = when (square.terrain) {
            Terrain.CENTER -> "000000"
            Terrain.PLAIN -> "ffff00"
            Terrain.FOREST -> "80c000"
            Terrain.WATER -> "00ffff"
            Terrain.GRASS -> "00ff00"
            Terrain.SWAMP -> "ff00ff"
            Terrain.MINE -> "808080"
        }
        unsafe {
            +"""
                <td bgcolor="#$background"> ${square.crowns} </td>
            """.trimIndent()
        }
    }
}


private fun P.showDomino(dominoList: List<Domino>, mapping: Map<Int, PlayerColor>, name: String) {
    if (dominoList.isEmpty()) {
        return
    }
    +name
    table {
        dominoList.forEachIndexed { index, domino ->
            tr {
                val kingColor = mapping[index]
                td {
                    +(kingColor?.shortName() ?: "------")
                }
                showSquare(domino.first)
                showSquare(domino.second)
            }
        }
    }
}

private fun HTML.generate() {
    head {
        title("Hello from Ktor!")
        style {
            unsafe {
                +"""
                    td {
                        font-size: 24pt
                    }
                 """.trimIndent()
            }
        }
        unsafe {
            +"<meta http-equiv=\"refresh\" content=\"2\">"
        }
    }
    body {
        h1 {
            +"Kingdomino!"
        }
        table {
            tr {
                for (color in game.players) {
                    td {
                        val kingdom = game.kingdom(color)
                        p {
                            +"Player: ${color.shortName()} ||"
                        }
                        p {
                            table {
                                for (row in kingdom.minY..kingdom.maxY) {
                                    tr {
                                        for (column in kingdom.minX..kingdom.maxX) {
                                            showSquare(kingdom.getSquare(Point(column, row)))
                                        }
                                    }
                                }
                                for (row in kingdom.maxY + 1..kingdom.minY + 4) {
                                    tr {
                                        for (column in kingdom.minX..kingdom.maxX) {
                                            td { +"-" }
                                        }
                                    }
                                }
                            }
                        }
                        p {
                            +"${game.score(color)}"
                        }
                    }
                }
            }
        }
        table {
            tr {
                td {
                    p {
                        showDomino(game.currentDomino, game.currentDominoMapping, name = "current")
                    }
                }
                td {
                    p {
                        showDomino(game.nextDomino, game.nextDominoMapping, name = "next")
                    }
                }
            }
        }
        p {
            when (game.state) {
                is GameState.Start -> {
                    +"Game isn't started!"
                }
                !is GameState.End -> {
                    if (stopped) {
                        +"Game paused!"
                    } else {
                        +"Game in process!"
                    }
                }
                else -> {
                    +"Game over!"
                }
            }
        }
        form("/", encType = FormEncType.textPlain, method = FormMethod.post) {
            acceptCharset = "utf-8"
            p {
                submitInput(name = "Start") {
                    value = if (playThread == null) "Начать" else "Продолжить"
                }
            }
            p {
                submitInput(name = "Stop") {
                    value = "Остановить"
                }
            }
        }
    }
}

fun main() {
    for (i in 1..3) {
        runner.addPlayer(SimpleThinkingPlayer())
    }
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        routing {
            get("/") {
                call.respondHtml {
                    generate()
                }
            }
            post("/") {
                val text = call.receiveText()
                if (text.startsWith("Start")) {
                    playGame(isStart = true)
                } else if (text.startsWith("Stop")) {
                    playGame(isStart = false)
                }
                call.respondHtml {
                    generate()
                }
            }
        }
    }.start(wait = true)
}

var playThread: Thread? = null

var stopped = false

private fun playGame(isStart: Boolean) {
    if (isStart) {
        if (playThread == null) {
            playThread = thread {
                while (game.state != GameState.End) {
                    runner.makeOneTurn()
                    Thread.sleep(2000)
                }
            }
        } else {
            playThread?.resume()
        }
        stopped = false
    } else {
        playThread?.suspend()
        stopped = true
    }
}