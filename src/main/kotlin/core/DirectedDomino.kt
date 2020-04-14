package core

data class DirectedDomino(val domino: Domino, val direction: Direction) {
    val first: Square get() = domino.first
    val second: Square get() = domino.second
}