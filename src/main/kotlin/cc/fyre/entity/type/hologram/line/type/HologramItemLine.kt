package cc.fyre.entity.type.hologram.line.type

import cc.fyre.entity.type.hologram.line.HologramLine
import com.squareup.moshi.JsonClass
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


@JsonClass(generateAdapter = true)
class HologramItemLine(var item: ItemStack,location: Location) : HologramLine(location) {

    //TODO

    override fun render(player: Player) {}
    override fun update(player: Player) {}
    override fun destroy(player: Player) {}

}