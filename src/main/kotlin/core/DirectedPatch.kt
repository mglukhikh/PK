package core

data class DirectedPatch(val patch: Patch, val direction: Direction) {
    val first: Square get() = patch.first
    val second: Square get() = patch.second
}