package core

class Game(val size: Int, val players: List<PlayerColor>) {

    private val deck = Deck()

    var nextPatches = deck.take(players.size)
        private set

    var nextPatchMapping = mutableMapOf<Int, PlayerColor>()
        private set

    var currentPatches = emptyList<Patch>()
        private set

    var currentPatchMapping = mutableMapOf<Int, PlayerColor>()
        private set

    private val kingdoms = players.associateWith { Kingdom(size) }

    fun mapNextPatch(player: PlayerColor, index: Int): Boolean {
        if (index !in nextPatches.indices) {
            throw AssertionError("Incorrect patch index: $index")
        }
        return nextPatchMapping.put(index, player) == null
    }

    fun takeCurrentPatch(index: Int): Pair<PlayerColor, Patch>? {
        val color = currentPatchMapping[index] ?: return null
        currentPatchMapping.remove(index)
        val patch = nextPatches[index]
        return color to patch
    }

    fun transferPatchesToCurrent(regenerateNext: Boolean) {
        currentPatches = nextPatches
        currentPatchMapping = nextPatchMapping
        nextPatches = if (regenerateNext) deck.take(players.size) else emptyList()
        nextPatchMapping.clear()
    }

    fun kingdom(player: PlayerColor) = kingdoms.getValue(player)

    fun addPatch(player: PlayerColor, point: Point, patch: DirectedPatch): Boolean {
        return kingdoms.getValue(player).addPatch(point, patch)
    }

    fun removeLastPatch(player: PlayerColor) {
        kingdoms.getValue(player).removeLastPatch()
    }

    fun scores(): Map<PlayerColor, Int> {
        return kingdoms.mapValues { (_, kingdom) ->
            kingdom.score()
        }
    }
}