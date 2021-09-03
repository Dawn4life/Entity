package cc.fyre.entity.type.npc

import cc.fyre.entity.Entity
import cc.fyre.entity.EntityHandler
import cc.fyre.entity.event.NPCNameTagStateEvent
import cc.fyre.entity.type.hologram.Hologram
import cc.fyre.entity.EntityVisibility
import cc.fyre.entity.util.PacketUtil
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.comphenix.protocol.wrappers.WrappedGameProfile
import com.comphenix.protocol.wrappers.WrappedSignedProperty
import com.squareup.moshi.JsonClass
import net.minecraft.server.v1_7_R4.MathHelper
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

@JsonClass(generateAdapter = true)
open class NPC(name: String,location: Location) : Entity(name,location,null) {

    val uuid: UUID = UUID.randomUUID()

    var swing = true
    var faces = false

    var hologram = Hologram("NPC_${this.name}_HOLOGRAM",location.clone().add(0.0,2.0,0.0),this.id)
    var commands = mutableListOf<String>()

    @Transient internal val facing = mutableSetOf<UUID>()

    var texture: String? = DEFAULT_SKIN_TEXTURE
    var signature: String? = DEFAULT_SKIN_SIGNATURE
    var skinUsername: String? = "SimplyTrash"

    var tabVisibility = EntityVisibility.VISIBLE
    var tagVisibility = EntityVisibility.VISIBLE

    var equipment = arrayOfNulls<ItemStack>(5)

    @Transient private var batId = EntityHandler.getNewEntityId()
    @Transient private var profile = WrappedGameProfile(this.uuid,this.name)

    init {
        this.profile.properties["textures"].add(WrappedSignedProperty.fromValues("textures",this.texture ?: "",this.signature ?: ""))
    }

    override fun sendCreatePacket(player: Player) {

        val watcher = WrappedDataWatcher()

        watcher.setObject(0,0.toByte())
        watcher.setObject(1,20.0F)
        watcher.setObject(2,this.name)
        watcher.setObject(3,0x01)
        watcher.setObject(4,0x00)
        watcher.setObject(5,0x01)
        watcher.setObject(6,20.0F)
        watcher.setObject(7,0)
        watcher.setObject(8,0.toByte())
        watcher.setObject(9,0.toByte())
        watcher.setObject(10,127.toByte())
        watcher.setObject(15,1.toByte())
        watcher.setObject(16,0x01)
        watcher.setObject(17,0F)

        var packet = PacketContainer(PacketType.Play.Server.PLAYER_INFO)

        packet.strings.write(0,this.name)
        packet.integers.write(0,PacketUtil.ADD_PLAYER)
        packet.integers.write(1,GameMode.CREATIVE.ordinal)
        packet.gameProfiles.write(0,this.profile)

        PacketUtil.sendPacket(player,packet)

        packet = PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN)

        packet.integers.write(0,this.id)
        packet.bytes.write(0,PacketUtil.convertYawOrPitch(this.location.yaw))
        packet.bytes.write(1,PacketUtil.convertYawOrPitch(this.location.pitch))
        packet.integers.write(1,MathHelper.floor(this.location.blockX * 32.0))
        packet.integers.write(2,MathHelper.floor(this.location.blockY * 32.0))
        packet.integers.write(3,MathHelper.floor(this.location.blockZ * 32.0))
        packet.gameProfiles.write(0,this.profile)
        packet.dataWatcherModifier.write(0,watcher)

        PacketUtil.sendPacket(player,packet)

        packet = PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION)
        packet.integers.write(0,this.id)
        packet.bytes.write(0,PacketUtil.convertYawOrPitch(this.location.yaw))

        PacketUtil.sendPacket(player,packet)

        packet = PacketContainer(PacketType.Play.Server.ENTITY_LOOK)
        packet.integers.write(0,this.id)
        packet.bytes.write(0,PacketUtil.convertYawOrPitch(this.location.yaw))
        packet.bytes.write(1,PacketUtil.convertYawOrPitch(this.location.pitch))
        packet.booleans.write(0,true)

        PacketUtil.sendPacket(player,packet)

        if (this.swing) {
            packet = PacketContainer(PacketType.Play.Server.ANIMATION)
            packet.integers.write(0,this.id)
            packet.integers.write(1, NPCAnimationType.SWING.id)

            PacketUtil.sendPacket(player,packet)
        }

        if (this.tagVisibility == EntityVisibility.HIDDEN) {
            Bukkit.getServer().scheduler.runTaskLater(EntityHandler.plugin,{this.ensureTagVisibility(player)},2L)
        }

        if (this.tabVisibility == EntityVisibility.HIDDEN) {
            Bukkit.getServer().scheduler.runTaskLater(EntityHandler.plugin,{
                packet = PacketContainer(PacketType.Play.Server.PLAYER_INFO)

                packet.strings.write(0,this.name)
                packet.integers.write(0,PacketUtil.REMOVE_PLAYER)
                packet.gameProfiles.write(0,this.profile)

                PacketUtil.sendPacket(player,packet)
            },20L)
        }

        packet = PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT)
        packet.integers.write(0,this.id)
        packet.bytes.write(0,PacketUtil.convertYawOrPitch(this.location.yaw))
        packet.bytes.write(1,PacketUtil.convertYawOrPitch(this.location.pitch))
        packet.integers.write(1,MathHelper.floor(this.location.blockX * 32.0))
        packet.integers.write(2,MathHelper.floor(this.location.blockY * 32.0))
        packet.integers.write(3,MathHelper.floor(this.location.blockZ * 32.0))

        PacketUtil.sendPacket(player,packet)

        this.sendEquipment(player)
        this.hologram.sendCreatePacket(player)
    }

    override fun sendUpdatePacket(player: Player) {}

    override fun sendRefreshPacket(player: Player) {
        this.sendDestroyPacket(player)
        this.sendCreatePacket(player)
    }

    override fun sendDestroyPacket(player: Player) {

        val legacy = PacketUtil.isLegacy(player)
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)

        val entityIds = mutableListOf<Int>()

        this.hologram.lines.forEach{

            if (legacy) {
                entityIds.add(it.horseId)
            }

            entityIds.add(it.skullId)
        }

        entityIds.add(this.id)

        packet.integerArrays.write(0,entityIds.toIntArray())

        PacketUtil.sendPacket(player,packet)
    }

    override fun setLocation(location: Location) {
        this.location = location
        this.hologram.setLocation(location.clone().add(0.0,if (this.tagVisibility == EntityVisibility.HIDDEN) HOLOGRAM_DISTANCE else HOLOGRAM_DISTANCE_WITH_NAME_TAG,0.0))
    }

    fun setSkin(player: Player) {

        val property = WrappedGameProfile.fromPlayer(player).properties["textures"].firstOrNull() ?: return

        this.texture = property.value
        this.signature = property.signature
        this.skinUsername = player.name

        val properties = this.profile.properties["textures"]

        properties.clear()
        properties.add(WrappedSignedProperty.fromValues("textures",this.texture ?: "",this.signature ?: ""))

        this.sendToAll{this.sendDestroyPacket(it);this.sendCreatePacket(it)}
    }

    fun setSkin(username: String?,texture: String?,signature: String?) {

        this.texture = texture
        this.signature = signature
        this.skinUsername = username

        val properties = this.profile.properties["textures"]

        properties.clear()
        properties.add(WrappedSignedProperty.fromValues("textures",this.texture ?: "",this.signature ?: ""))

        this.sendToAll{this.sendDestroyPacket(it);this.sendCreatePacket(it)}
    }

    fun updateTabVisibility(visibility: EntityVisibility) {

        if (this.tabVisibility == visibility) {
            return
        }

        val packet = PacketContainer(PacketType.Play.Server.PLAYER_INFO)

        packet.strings.write(0,this.name)
        packet.integers.write(0,if (visibility == EntityVisibility.HIDDEN) PacketUtil.REMOVE_PLAYER else PacketUtil.ADD_PLAYER)
        packet.integers.write(1,GameMode.CREATIVE.ordinal)
        packet.gameProfiles.write(0,this.profile)

        this.tabVisibility = visibility
        this.sendToAll{PacketUtil.sendPacket(it,packet)}
    }

    fun updateTagVisibility(visibility: EntityVisibility) {

        if (this.tagVisibility == visibility) {
            return
        }

        this.hologram.setLocation(this.location.clone().add(0.0,if (this.tagVisibility == EntityVisibility.HIDDEN) HOLOGRAM_DISTANCE else HOLOGRAM_DISTANCE_WITH_NAME_TAG,0.0))

        this.tagVisibility = visibility

        Bukkit.getPluginManager().callEvent(NPCNameTagStateEvent(this,visibility))
    }

    fun getEquipment(slot: Int):ItemStack? {
        return this.equipment[slot]
    }

    fun setEquipment(slot: Int,item: ItemStack?) {

        if (this.equipment[slot] == item) {
            return
        }

        val packet = PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT)

        packet.integers.write(0,this.id)
        packet.integers.write(1,slot)
        packet.itemModifier.write(0,item)

        this.equipment[slot] = item
        this.sendToAll{PacketUtil.sendPacket(it,packet)}
    }

    fun sendEquipment(player: Player) {

        for (i in 0..this.equipment.lastIndex) {

            if (this.equipment[i] == null) {
                continue
            }

            val packet = PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT)

            packet.integers.write(0,this.id)
            packet.integers.write(1,i)
            packet.itemModifier.write(0,this.equipment[i])

            PacketUtil.sendPacket(player,packet)
        }

    }

    internal fun ensureTagVisibility(player: Player) {

        // Can we somehow send a 1.8 scoreboard packet with team nametagVisbility set to NEVER?

        if (this.tabVisibility == EntityVisibility.VISIBLE) {

            val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)

            packet.integerArrays.write(0,intArrayOf(this.batId))

            PacketUtil.sendPacket(player,packet)
            return
        }

        var packet = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING)

        packet.integers.write(0,this.batId)
        packet.integers.write(1,BAT_TYPE_ID)
        packet.integers.write(2,(this.location.x * 32.0).toInt())
        packet.integers.write(3,MathHelper.floor((this.location.y + 55.0) * 32.0))
        packet.integers.write(4,(this.location.z * 32.0).toInt())

        val watcher = WrappedDataWatcher()

        watcher.setObject(0,(0 or 1 shl 5).toByte())
        watcher.setObject(1,300.toShort())
        watcher.setObject(12,-1700000)

        packet.dataWatcherModifier.write(0,watcher)

        PacketUtil.sendPacket(player,packet)

        packet = PacketContainer(PacketType.Play.Server.ATTACH_ENTITY)

        packet.integers.write(0,0)
        packet.integers.write(1,this.batId)
        packet.integers.write(2,this.id)

        PacketUtil.sendPacket(player,packet)
    }

    companion object {

        val BAT_TYPE_ID = org.bukkit.entity.EntityType.BAT.typeId.toInt()

        const val FACE_DISTANCE = 5.0
        const val NAME_TAG_TEAM = "NPC_NAME_TAG"

        const val HOLOGRAM_DISTANCE = 0.095
        const val HOLOGRAM_DISTANCE_WITH_NAME_TAG = 0.3

        const val DEFAULT_SKIN_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTYxNTQ5MTYzMjUwMCwKICAicHJvZmlsZUlkIiA6ICIwZjdhNGU5ODdmNTA0NjU0ODMzNGVlMTcxMDkyNWFjZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTaW1wbHlUcmFzaCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xYzZhMzU5MjBlZDYwMTcwNzAxODg5MjZjNzgzMjkwYmMyNjZlODE4N2ZmNTVmMWNkODMxZjIyNTQ2MThiZjcxIgogICAgfQogIH0KfQ=="
        const val DEFAULT_SKIN_SIGNATURE = "fKm4SGhkK+aNM2OS9cf8DOW32H7YdYg0PPfwVVOPMgycP5+PP7i/FQ/4iZdWmeAgn35WQFiTf7Y+4SPxBS2xGGAzHl2fEjz7HarstnU51Ii0UIA1HxzysOzQdS1Z2nZDn9HeP4/RqHp1a4Vf2alRygYxPXMCp3sHKhCJq17fqDHxxXa5LwG4jc2s9w/S36QChQ2zNaGmziwHDbg6LDzwY55coQeyKKxRaSf+mNr41nrAnVZdNMC9dl6ZcPkIyTCuAJcqzC+RDdnpUX3hTNCsJG5b/v1yzNwSp+tHNBvYVXJlYhemyIhk1Lk9ZVu97HSfsodyGNCbFxH3FdVlqg3woWU3sk5lyZhh4wlX3zH8n52q++ORsX2k/d12Cv8Y3KZSA+URblm4jGz+D2dKxT4+fwMUkWJ5SqdOAcbphZMD7w5a0ozD2SwzSLQUgq+BIqkXgNoz3303t7RE1ECjxjWDJjed/afEAb8qBkzl/ONkf8Yhh+ly1INeTYlZ1s93ck+IB2UXK5mTUeA1o9TMyOftMNRoPbaEDJuAGLT54/ryM6Gxq18iwaldidgXHyu1rN64UrY1xYZCFWnpvzQuQG1XrqUe5TOdFDZG6cQHk5EzAO514Q8KqZ5N+eFGWARtFleRHfYore0mNIThQ54QJ6aTAV7Eev2bCuwnzYg5Dd1O1fM="
    }

}