package core

data class Square(val terrain: Terrain, val crowns: Int = 0) {
    constructor(index: Int, crowns: Int = 0):
            this(Terrain.values()[index], crowns)

    constructor(code: Int):
            this(code / 10, code % 10)

    fun sameWith(other: Square?): Boolean =
        terrain.sameWith(other?.terrain)
}

