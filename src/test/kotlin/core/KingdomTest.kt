package core

import junit.framework.Assert.*
import org.junit.Test

class KingdomTest {

    @Test
    fun testSimple() {
        val kingdom = Kingdom(5)
        assertFalse(kingdom.addPatch(Point(0, 0), 0.toDeckPatch(Direction.TO_RIGHT)))
        assertFalse(kingdom.addPatch(Point(1, 1), 0.toDeckPatch(Direction.TO_RIGHT)))
        assertTrue(kingdom.addPatch(Point(0, 1), 0.toDeckPatch(Direction.TO_RIGHT)))
        assertEquals(0, kingdom.score())
        assertTrue(kingdom.addPatch(Point(-1, 1), 18.toDeckPatch(Direction.TO_DOWN)))
        assertEquals(3, kingdom.score())
    }
}