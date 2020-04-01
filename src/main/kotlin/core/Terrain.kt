package core

enum class Terrain {
    CENTER,
    GRASS,
    PLAIN,
    FOREST,
    WATER,
    DIRT,
    CAVE;

    fun canAddTo(other: Terrain): Boolean =
        this == other || other == CENTER
}