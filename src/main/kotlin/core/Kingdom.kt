package core

import java.lang.AssertionError

class Kingdom(val size: Int) {
    val squares = mutableMapOf(Point(0, 0) to Square(Terrain.CENTER))

    private val patches = mutableListOf<Pair<Point, DirectedPatch>>()

    private val minX: Int get() = squares.keys.minBy { it.x }!!.x

    private val minY: Int get() = squares.keys.minBy { it.y }!!.y

    private val maxX: Int get() = squares.keys.maxBy { it.x }!!.x

    private val maxY: Int get() = squares.keys.maxBy { it.y }!!.y

    val dimension: Int get() = maxOf(maxX - minX, maxY - minY) + 1

    private val overflown: Boolean get() = dimension > size

    fun isPatchApplicable(point: Point, patch: DirectedPatch): Boolean {
        if (point in squares) return false
        val secondPoint = point + patch.direction
        if (secondPoint in squares) return false
        if (Direction.values().none { direction ->
                patch.first.sameWith(squares[point + direction]) ||
                        patch.second.sameWith(squares[secondPoint + direction])
            }
        ) return false
        val minX = minOf(minX, point.x, secondPoint.x)
        val maxX = maxOf(maxX, point.x, secondPoint.x)
        val minY = minOf(minY, point.y, secondPoint.y)
        val maxY = maxOf(maxY, point.y, secondPoint.y)
        if (maxX - minX >= size || maxY - minY >= size) return false
        return true
    }

    fun addPatch(point: Point, patch: DirectedPatch): Boolean {
        if (isPatchApplicable(point, patch)) return false
        val secondPoint = point + patch.direction

        patches += (point to patch)
        squares[point] = patch.first
        squares[secondPoint] = patch.second

        if (overflown) {
            throw AssertionError("Overflown! Should not be here!")
        }

        return true
    }

    fun removeLastPatch() {
        if (patches.isEmpty()) {
            throw IllegalStateException("No patch to remove")
        }
        val (point, patch) = patches.last()
        patches.removeAt(patches.size - 1)
        squares.remove(point)
        squares.remove(point + patch.direction)
    }

    fun score(): Int {
        var result = 0
        val visitedPoints = mutableSetOf<Point>()
        for ((point, square) in squares) {
            if (point in visitedPoints) continue
            visitedPoints += point
            val terrain = square.terrain
            if (terrain == Terrain.CENTER) continue
            var crowns = 0
            var points = 0
            val queue = java.util.ArrayDeque<Point>()
            queue.add(point)
            while (queue.isNotEmpty()) {
                val wavePoint = queue.poll()
                points++
                crowns += squares[wavePoint]!!.crowns
                for (direction in Direction.values()) {
                    val nextPoint = wavePoint + direction
                    if (squares[nextPoint]?.terrain != terrain) continue
                    if (nextPoint in visitedPoints) continue
                    visitedPoints += nextPoint
                    queue.add(nextPoint)
                }
            }
            result += crowns * points
        }
        return result
    }

}