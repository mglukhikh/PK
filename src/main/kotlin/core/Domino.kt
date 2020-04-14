package core

data class Domino(val number: Int, val first: Square, val second: Square) {
    constructor(number: Int, firstCode: Int, secondCode: Int):
            this(number, Square(firstCode), Square(secondCode))
}