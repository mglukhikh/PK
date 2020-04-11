package core

enum class Direction(val dx: Int, val dy: Int) {
    TO_RIGHT(1, 0),
    TO_UP(0, 1),
    TO_LEFT(-1, 0),
    TO_DOWN(0, -1);

    fun toPoint() = Point(dx, dy)
}