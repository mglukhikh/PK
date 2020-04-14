package core

import junit.framework.Assert.*
import org.junit.Test

class KingdomTest {

    @Test
    fun testSimple() {
        val kingdom = Kingdom(5)
        assertFalse(kingdom.addDomino(Point(0, 0), 0.toDeckDomino(Direction.TO_RIGHT)))
        assertFalse(kingdom.addDomino(Point(1, 1), 0.toDeckDomino(Direction.TO_RIGHT)))
        assertTrue(kingdom.addDomino(Point(0, 1), 0.toDeckDomino(Direction.TO_RIGHT)))
        assertEquals(0, kingdom.score())
        assertTrue(kingdom.addDomino(Point(-1, 1), 18.toDeckDomino(Direction.TO_DOWN)))
        assertEquals(3, kingdom.score())
    }
}