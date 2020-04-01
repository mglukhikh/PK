package core

enum class Terrain {
    CENTER,
    PLAIN,
    FOREST,
    WATER,
    GRASS,
    SWAMP,
    MINE;

    fun sameWith(other: Terrain?): Boolean =
        this == other || other == CENTER
}