package core

import java.lang.AssertionError

class Kingdom(private val size: Int) {
    private val squares = mutableMapOf(Point(0, 0) to Square(Terrain.CENTER))

    private val dominos = mutableListOf<Pair<Point, DirectedDomino>>()

    val minX: Int get() = squares.keys.minByOrNull { it.x }!!.x

    val minY: Int get() = squares.keys.minByOrNull { it.y }!!.y

    val maxX: Int get() = squares.keys.maxByOrNull { it.x }!!.x

    val maxY: Int get() = squares.keys.maxByOrNull { it.y }!!.y

    private val dimension: Int get() = maxOf(maxX - minX, maxY - minY) + 1

    private val overflown: Boolean get() = dimension > size

    fun getSquare(point: Point): Square? = squares[point]

    fun isDominoApplicable(point: Point, domino: DirectedDomino): Boolean {
        if (point in squares) return false
        val secondPoint = point + domino.direction
        if (secondPoint in squares) return false
        if (Direction.values().none { direction ->
                domino.first.sameWith(squares[point + direction]) ||
                        domino.second.sameWith(squares[secondPoint + direction])
            }
        ) return false
        val minX = minOf(minX, point.x, secondPoint.x)
        val maxX = maxOf(maxX, point.x, secondPoint.x)
        val minY = minOf(minY, point.y, secondPoint.y)
        val maxY = maxOf(maxY, point.y, secondPoint.y)
        if (maxX - minX >= size || maxY - minY >= size) return false
        return true
    }

    fun addDomino(point: Point, domino: DirectedDomino): Boolean {
        if (!isDominoApplicable(point, domino)) return false
        val secondPoint = point + domino.direction

        dominos += (point to domino)
        squares[point] = domino.first
        squares[secondPoint] = domino.second

        if (overflown) {
            throw AssertionError("Overflown! Should not be here!")
        }

        return true
    }

    fun removeLastDomino() {
        if (dominos.isEmpty()) {
            throw IllegalStateException("No domino to remove")
        }
        val (point, domino) = dominos.last()
        dominos.removeAt(dominos.size - 1)
        squares.remove(point)
        squares.remove(point + domino.direction)
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