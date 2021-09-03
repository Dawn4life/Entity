package cc.fyre.entity.listener

import cc.fyre.entity.EntityHandler
import cc.fyre.entity.Entity
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.*


object EntityVisibilityListener : Listener {

    /* Handling in EntityThread
    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {

        for (entity in EntityHandler.getAllEntities()) {

            if (entity.visibility != EntityVisibility.VISIBLE) {
                continue
            }

            if (entity.location.world != event.player.world) {
                continue
            }

            if (entity.getDistanceSquared(event.player.location) > Entity.DISTANCE) {
                continue
            }

            if (entity.viewers.contains(event.player.uniqueId)) {
                continue
            }

            if (entity is NPC) {
                entity.setSkin(event.player)
            }

            entity.viewers.add(event.player.uniqueId)
            entity.sendCreatePacket(event.player)
        }

    }*/

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        EntityHandler.getAllEntities().filter{it.viewers.contains(event.player.uniqueId)}.forEach{it.viewers.remove(event.player.uniqueId)}
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerChangedWorldEvent(event: PlayerChangedWorldEvent) {

        Bukkit.getServer().scheduler.runTaskLaterAsynchronously(EntityHandler.plugin,{

            EntityHandler.getAllEntities().forEach{

                if (it.getDistanceSquared(event.player.location) > Entity.DISTANCE) {
                    return@forEach
                }

                it.viewers.add(event.player.uniqueId)
                it.sendCreatePacket(event.player)
            }

        },20L)

    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerRespawnEvent(event: PlayerRespawnEvent) {

        Bukkit.getServer().scheduler.runTaskLaterAsynchronously(EntityHandler.plugin,{

            EntityHandler.getAllEntities().forEach{

                if (it.viewers.contains(event.player.uniqueId)) {
                    it.viewers.remove(event.player.uniqueId)
                }

                if (it.getDistanceSquared(event.player.location) > Entity.DISTANCE) {
                    return@forEach
                }

                it.viewers.add(event.player.uniqueId)
                it.sendCreatePacket(event.player)
            }

        },20L)

    }


}