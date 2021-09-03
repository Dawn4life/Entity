package cc.fyre.entity.type.hologram.line.type

import cc.fyre.entity.EntityHandler
import cc.fyre.entity.type.hologram.line.HologramLine
import cc.fyre.entity.util.PacketUtil
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer

import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.comphenix.protocol.wrappers.WrappedWatchableObject
import com.google.common.collect.Iterators
import com.squareup.moshi.JsonClass
import net.minecraft.server.v1_7_R4.MathHelper
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.EntityType

import org.bukkit.entity.Player

@JsonClass(generateAdapter = true)
class HologramTextLine(var text: String,location: Location) : HologramLine(location) {

    @Transient private var blank = this.text == " " || this.text.equals("blank",true)
    @Transient private var textLowerCase = this.text.lowercase()

    override fun render(player: Player) {

        var text = this.text

        for (map in EntityHandler.getAdapters().map{it.resolve(player)}) {

            for (entry in map) {

                if (!this.textLowerCase.contains(entry.key.lowercase())) {
                    continue
                }

                text = text.replace(entry.key,entry.value.toString(),true)
            }

        }

        if (!PacketUtil.isLegacy(player)) {
            PacketUtil.sendPacket(player,this.createArmorStand())
            return
        }

        PacketUtil.sendPacket(player,this.createSkull())
        PacketUtil.sendPacket(player,this.createHorse())

        val packet = PacketContainer(PacketType.Play.Server.ATTACH_ENTITY)

        packet.integers.write(1,this.horseId)
        packet.integers.write(2,this.skullId)

        PacketUtil.sendPacket(player,packet)
    }

    override fun update(player: Player) {

        var text = this.text

        for (map in EntityHandler.getAdapters().map{it.resolve(player)}) {

            for (entry in map) {

                if (!this.textLowerCase.contains(entry.key.lowercase())) {
                    continue
                }

                text = text.replace(entry.key,entry.value.toString(),true)
            }

        }

        val legacy = PacketUtil.isLegacy(player)
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)

        val watcher = WrappedDataWatcher()

        watcher.setObject(if (legacy) 10 else 2,text)
        watcher.setObject(if (legacy) 11 else 3,(if (this.blank) 0 else 1).toByte())

        packet.integers.write(0,if (legacy) this.horseId else this.skullId)
        packet.watchableCollectionModifier.write(0,listOf(*Iterators.toArray(watcher.iterator(),WrappedWatchableObject::class.java)))

        PacketUtil.sendPacket(player,packet)
    }

    private fun createSkull():PacketContainer {

        val toReturn = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY)

        toReturn.integers.write(0,this.skullId)
        toReturn.integers.write(1,(this.location.x * 32.0).toInt())
        toReturn.integers.write(2,MathHelper.floor((this.location.y - 0.13 + 55.0) * 32.0))
        toReturn.integers.write(3,(this.location.z * 32.0).toInt())
        toReturn.integers.write(9,66)

        return toReturn
    }

    private fun createHorse():PacketContainer {

        val toReturn = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING)

        toReturn.integers.write(0,this.horseId)
        toReturn.integers.write(1,EntityType.HORSE.typeId.toInt())
        toReturn.integers.write(2,(this.location.x * 32.0).toInt())
        toReturn.integers.write(3,MathHelper.floor((this.location.y + 55.0) * 32.0))
        toReturn.integers.write(4,(this.location.z * 32.0).toInt())

        val watcher = WrappedDataWatcher()

        watcher.setObject(0,0.toByte())
        watcher.setObject(1,300.toShort())
        watcher.setObject(10,ChatColor.translateAlternateColorCodes('&',this.text))
        watcher.setObject(11,(if (this.blank) 0 else 1).toByte())
        watcher.setObject(12,-1700000)

        toReturn.dataWatcherModifier.write(0,watcher)

        return toReturn
    }

    private fun createArmorStand():PacketContainer {

        val watcher = WrappedDataWatcher()

        watcher.setObject(0,32.toByte()) // Invisible
        watcher.setObject(2,ChatColor.translateAlternateColorCodes('&',text))
        watcher.setObject(3,(if (this.blank) 0 else 1).toByte())

        val toReturn = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING)

        toReturn.bytes.write(0,PacketUtil.convertYawOrPitch(this.location.yaw))
        toReturn.bytes.write(1,PacketUtil.convertYawOrPitch(this.location.pitch))

        toReturn.integers.write(0,this.skullId)
        toReturn.integers.write(1, ARMOR_STAND_ID)
        toReturn.integers.write(2,MathHelper.floor((this.location.x * 32.0)))
        toReturn.integers.write(3,MathHelper.floor((this.location.y * 32.0)))
        toReturn.integers.write(4,MathHelper.floor((this.location.z * 32.0)))

        toReturn.dataWatcherModifier.write(0,watcher)

        return toReturn
    }

    override fun destroy(player: Player) {

        val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)

        packet.integerArrays.write(0,if (PacketUtil.isLegacy(player)) intArrayOf(this.skullId,this.horseId) else intArrayOf(this.skullId))

        PacketUtil.sendPacket(player,packet)
    }

    fun updateText(value: String) {
        this.text = value
        this.blank = this.text.equals("blank",true)
        this.textLowerCase = value.lowercase()
    }

}