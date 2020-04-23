package ktor

import core.Point
import core.Terrain
import game.Game
import game.GameRunner
import game.GameState
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
                        style {
                            unsafe {
                                +"""
                                    td {
                                        font-size: 18pt
                                    }
                                """.trimIndent()
                            }
                        }
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
                                                val square = kingdom.getSquare(Point(column, row))
                                                if (square == null) {
                                                    td { +"-" }
                                                } else {
                                                    val background = when (square.terrain) {
                                                        Terrain.CENTER -> "000000"
                                                        Terrain.PLAIN -> "ffff00"
                                                        Terrain.FOREST -> "008000"
                                                        Terrain.WATER -> "0000ff"
                                                        Terrain.GRASS -> "00ff00"
                                                        Terrain.SWAMP -> "808000"
                                                        Terrain.MINE -> "808080"
                                                    }
                                                    unsafe {
                                                        +"""
                                                            <td bgcolor="#$background">
                                                                ${square.crowns} 
                                                            </td>
                                                        """.trimIndent()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            p {
                                +"Score: ${game.score(color)}"
                            }
                        }
                        p {
                            if (game.state !is GameState.End) {
                                +"Game in process!"
                            } else {
                                +"Game over!"
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