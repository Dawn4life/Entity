package cc.fyre.entity.type.hologram

import cc.fyre.entity.EntityHandler
import cc.fyre.entity.Entity
import cc.fyre.entity.type.hologram.line.HologramLine
import cc.fyre.entity.type.hologram.line.type.HologramItemLine
import cc.fyre.entity.type.hologram.line.type.HologramTextLine
import cc.fyre.entity.util.PacketUtil
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.squareup.moshi.JsonClass

import org.bukkit.Location

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@JsonClass(generateAdapter = true)
class Hologram(name: String,location: Location,parent: Int? = null) : Entity(name,location,parent) {

    var lines = mutableListOf<HologramLine>()

    override fun sendCreatePacket(player: Player) {
        this.lines.forEach{it.render(player)}
    }

    override fun sendUpdatePacket(player: Player) {
        this.lines.forEach{it.update(player)}
    }

    override fun sendRefreshPacket(player: Player) {
        this.sendDestroyPacket(player)
        this.sendCreatePacket(player)
    }

    override fun sendDestroyPacket(player: Player) {

        val legacy = PacketUtil.isLegacy(player)
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)

        val entityIds = mutableListOf<Int>()

        this.lines.forEach{

            if (legacy) {
                entityIds.add(it.horseId)
            }

            entityIds.add(it.skullId)
        }

        packet.integerArrays.write(0,entityIds.toIntArray())

        PacketUtil.sendPacket(player,packet)
    }

    operator fun set(index: Int,text: String) {
        this.setText(index,text)
    }

    operator fun set(index: Int,item: ItemStack) {
        this.setItem(index,item)
    }

    fun addText(text: String) {

        for (line in this.lines) {
            line.location.add(0.0, TEXT_IN_BETWEEN_DISTANCE,0.0)
        }

        this.lines.add(HologramTextLine(text,this.location.clone().subtract(0.0, TEXT_IN_BETWEEN_DISTANCE,0.0)))
        this.sendToAll{this.sendRefreshPacket(it)}
    }

    fun addItem(item: ItemStack) {

        for (line in this.lines) {
            line.location.add(0.0, TEXT_IN_BETWEEN_DISTANCE,0.0)
        }

        this.lines.add(HologramItemLine(item,this.location.clone().subtract(0.0, TEXT_IN_BETWEEN_DISTANCE,0.0)))
        this.sendToAll{this.sendRefreshPacket(it)}
    }

    fun setText(index: Int,text: String) {

        if (index > this.lines.lastIndex) {
            this.addText(text)
            return
        }

        val line = this.lines[index]

        if (line is HologramTextLine) {
            line.updateText(text)
            this.sendToAll{line.update(it)}
            return
        }

        this.lines[index] = HologramTextLine(text,line.location.clone().add(0.0, TEXT_IN_BETWEEN_DISTANCE,0.0))
        this.sendToAll{line.destroy(it);this.lines[index].render(it)}
    }

    fun setItem(index: Int,item: ItemStack) {

        if (index > this.lines.lastIndex) {
            this.addItem(item)
            return
        }

        val line = this.lines[index]

        if (line is HologramItemLine) {
            line.item = item
            this.sendToAll{line.update(it)}
            return
        }

        this.lines[index] = HologramItemLine(item,line.location.clone().subtract(0.0, TEXT_IN_BETWEEN_DISTANCE,0.0))
        this.sendToAll{line.destroy(it);this.lines[index].render(it)}
    }

    fun remove(index: Int) {

        if (index > this.lines.lastIndex) {
            return
        }

        val removed = this.lines.removeAt(index)

        this.sendToAll{removed.destroy(it)}

        val toReload = this.lines.withIndex().filter{it.index >= index}.map{it.value}

        this.sendToAll{toReload.forEach{hologram -> hologram.destroy(it);hologram.render(it)}}
    }

    override fun setLocation(location: Location) {
        this.sendToAll{this.sendDestroyPacket(it)}

        this.lines.forEach{
            it.location.x = location.x
            it.location.z = location.z
        }

        this.location = location

        //TODO PacketPlayOutEntityTeleport

        this.sendToAll{this.sendCreatePacket(it)}
    }

    override fun sendToAll(lambda: (player: Player) -> Unit) {

        if (this.parent == null) {
            super.sendToAll(lambda)
            return
        }

       EntityHandler.getEntityById(this.parent!!)?.sendToAll(lambda) ?: super.sendToAll(lambda)
    }

    companion object {

        const val TEXT_IN_BETWEEN_DISTANCE = 0.23
        const val ITEM_IN_BETWEEN_DISTANCE = 0.46

    }
}