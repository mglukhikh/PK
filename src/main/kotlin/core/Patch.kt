package core

data class Patch(val number: Int, val first: Square, val second: Square) {
    constructor(number: Int, firstCode: Int, secondCode: Int):
            this(number, Square(firstCode), Square(secondCode))
}