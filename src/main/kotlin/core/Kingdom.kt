package core

class Kingdom(val size: Int) {
    val capacity = 2 * size + 1

    val squares = Array(capacity) { Array<Square?>(capacity) { null } }

    init {
        squares[size][size] = Square(Terrain.CENTER)
    }
}