package game

import core.PlayerColor
import junit.framework.Assert.assertEquals
import org.junit.Test
import player.BrainDeadPlayer

class GameTest {

    @Test
    fun simpleTest() {
        val runner = GameRunner(5, 4)
        for (i in 1..4) {
            runner.addPlayer(BrainDeadPlayer())
        }
        val result = runner.play()
        assertEquals(0, result[PlayerColor.YELLOW])
        assertEquals(0, result[PlayerColor.RED])
        assertEquals(0, result[PlayerColor.GREEN])
        assertEquals(0, result[PlayerColor.BLUE])
    }
}