package cc.fyre.entity.listener

import cc.fyre.entity.EntityHandler
import cc.fyre.entity.event.EntityInteractEvent
import cc.fyre.entity.type.npc.NPC

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import org.bukkit.Bukkit
import org.bukkit.event.Listener

object EntityPacketListener : PacketAdapter(EntityHandler.plugin,PacketType.Play.Client.USE_ENTITY),Listener {

    //TODO add cooldown

    init {
        ProtocolLibrary.getProtocolManager().addPacketListener(this)
    }

    override fun onPacketReceiving(event: PacketEvent) {

        if (event.packet.type != PacketType.Play.Client.USE_ENTITY) {
            return
        }

        val entity = EntityHandler.getEntityById(event.packet.integers.read(0)) ?: return

        if (entity.location.distanceSquared(event.player.location) > 6.0) {
            return
        }

        if (event.packet.entityUseActions.values.isEmpty()) {
            return
        }

        val action = event.packet.entityUseActions.read(0)

        if (action == EnumWrappers.EntityUseAction.ATTACK) {
            entity.onLeftClick(event.player)
        } else {

            if (entity is NPC) {
                entity.commands.forEach{event.player.chat("/$it")}
            }

            entity.onRightClick(event.player)
        }

        Bukkit.getServer().pluginManager.callEvent(EntityInteractEvent(entity,event.player,if (action == EnumWrappers.EntityUseAction.ATTACK) EntityInteractEvent.EntityInteractAction.LEFT_CLICK else EntityInteractEvent.EntityInteractAction.RIGHT_CLICK))
    }

}