package core

enum class Direction(val dx: Int, val dy: Int) {
    RIGHT(1, 0),
    UP(0, 1),
    LEFT(-1, 0),
    DOWN(0, -1);

    fun toPoint() = Point(dx, dy)
}