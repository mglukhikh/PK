package core

import junit.framework.Assert.*
import org.junit.Test

class KingdomTest {

    @Test
    fun testSimple() {
        val kingdom = Kingdom(5)
        assertFalse(kingdom.addPatch(Point(0, 0), 0.toDeckPatch(Direction.RIGHT)))
        assertFalse(kingdom.addPatch(Point(1, 1), 0.toDeckPatch(Direction.RIGHT)))
        assertTrue(kingdom.addPatch(Point(0, 1), 0.toDeckPatch(Direction.RIGHT)))
        assertEquals(0, kingdom.score())
        assertTrue(kingdom.addPatch(Point(-1, 1), 18.toDeckPatch(Direction.DOWN)))
        assertEquals(3, kingdom.score())
    }
}