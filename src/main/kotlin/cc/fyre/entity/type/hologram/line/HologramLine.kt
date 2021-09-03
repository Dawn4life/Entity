package cc.fyre.entity.type.hologram.line

import cc.fyre.entity.EntityHandler
import com.squareup.moshi.JsonClass
import org.bukkit.Location
import org.bukkit.entity.Player

@JsonClass(generateAdapter = false)
abstract class HologramLine(var location: Location) {

    @Transient internal val skullId = EntityHandler.getNewEntityId()
    @Transient internal val horseId = EntityHandler.getNewEntityId()

    abstract fun render(player: Player)
    abstract fun update(player: Player)
    abstract fun destroy(player: Player)

    companion object {

        const val ARMOR_STAND_ID = 30

    }
}