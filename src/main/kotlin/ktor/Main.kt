package ktor

import core.Point
import game.Game
import game.GameRunner
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import player.SimpleThinkingPlayer
import kotlin.concurrent.thread

private val runner = GameRunner(5, 3)

private val game: Game
    get() = runner.game

fun main() {
    thread {
        playGame()
    }
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        routing {
            get("/") {
                call.respondHtml {
                    head {
                        title("Hello from Ktor!")
                        unsafe {
                            +"<meta http-equiv=\"refresh\" content=\"2\">"
                        }
                    }
                    body {
                        p {
                            +"Kingdomino!"
                        }
                        for (color in game.players) {
                            val kingdom = game.kingdom(color)
                            p {
                                table {
                                    for (row in kingdom.minY..kingdom.maxY) {
                                        tr {
                                            for (column in kingdom.minX..kingdom.maxX) {
                                                td {
                                                    val square = kingdom.getSquare(Point(column, row))
                                                    if (square == null) {
                                                        +"--------"
                                                    } else {
                                                        +square.toString()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}

private fun playGame() {
    for (i in 1..3) {
        runner.addPlayer(SimpleThinkingPlayer())
    }
    Thread.sleep(5000)
    runner.play(1000)
}