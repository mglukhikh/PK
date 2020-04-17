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
import player.BrainDeadPlayer
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
                    }
                    body {
                        p {
                            +"Kingdomino!"
                        }
                        for (color in game.players) {
                            val kingdom = game.kingdom(color)
                            p {
                                table {
                                    for (row in -5..5) {
                                        tr {
                                            for (column in -5..5) {
                                                td {
                                                    val square = kingdom.getSquare(Point(column, row))
                                                    if (square == null) {
                                                        +"--------"
                                                    } else {
                                                        +square.terrain.name.toLowerCase()
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
        runner.addPlayer(BrainDeadPlayer())
    }
    Thread.sleep(10000)
    runner.play(5000)
}