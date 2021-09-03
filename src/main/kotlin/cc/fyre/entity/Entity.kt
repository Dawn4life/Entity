package cc.fyre.entity

import com.squareup.moshi.JsonClass

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.io.File
import java.util.*

@JsonClass(generateAdapter = false)
abstract class Entity(var name: String,internal var location: Location,@Transient var parent: Int? = null) {

    var visibility = EntityVisibility.VISIBLE

    @Transient var id = EntityHandler.getNewEntityId()
    @Transient var file = File("${EntityRepository.container.absolutePath}/${this.getSerializedName()}.json")

    @Transient var tick = 0
    @Transient var persistent = true

    @Transient val viewers = mutableSetOf<UUID>()

    fun getLocation():Location {
        return this.location
    }

    open fun setLocation(location: Location) {
        this.location = location
    }

    fun getDistance(location: Location):Double {
        return this.location.distance(location)
    }

    fun getDistanceSquared(location: Location):Double {
        return this.location.distanceSquared(location)
    }

    fun updateVisibility(visibility: EntityVisibility) {
        this.visibility = visibility
        this.visibility.action.test(this)
    }

    private fun getSerializedName():String {
        return this.name
    }

    abstract fun sendCreatePacket(player: Player)
    abstract fun sendUpdatePacket(player: Player)
    abstract fun sendRefreshPacket(player: Player)
    abstract fun sendDestroyPacket(player: Player)

    open fun sendToAll(lambda: (player: Player) -> Unit) {
        this.viewers.mapNotNull{Bukkit.getServer().getPlayer(it)}.forEach(lambda)
    }

    open fun onTick() {}
    open fun onLeftClick(player: Player) {}
    open fun onRightClick(player: Player) {}

    companion object {

        const val DISTANCE = 1600.0
        const val MOVE_METADATA = "ENTITY_NAME"

        private val BLOCK_HEIGHTS = hashMapOf(
                "SLAB" to 0.5,
                "TRAPDOOR" to 0.2
        )

        fun getBlockHeight(type: Material):Double? {
            return BLOCK_HEIGHTS.entries.firstOrNull{type.name.endsWith(it.key,true)}?.value
        }
    }

}