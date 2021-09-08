package cc.fyre.entity.thread

import cc.fyre.entity.Entity
import cc.fyre.entity.EntityHandler
import cc.fyre.entity.EntityVisibility
import org.bukkit.Bukkit

object EntityThread : Thread("Shard - Entity Thread") {

    private var tick = 0

    override fun run() {

        while (true) {

            if (++this.tick > 20) {
                this.tick = 0
                this.tickViewers()
            }

            EntityHandler.getAllEntities().forEach{entity ->

                if (++entity.tick >= 20) {
                    entity.tick = 0
                }

                entity.onTick()
            }

            sleep(50)
        }

    }

    private fun tickViewers() {

        for (player in Bukkit.getServer().onlinePlayers) {

            for (entity in EntityHandler.getAllEntities()) {

                if (entity.visibility == EntityVisibility.HIDDEN) {
                    continue
                }

                val visible = entity.location.world == player.world && entity.getDistanceSquared(player.location) <= Entity.DISTANCE

                if (!visible) {

                    if (!entity.viewers.contains(player.uniqueId)) {
                        continue
                    }

                    entity.viewers.remove(player.uniqueId)
                    entity.sendDestroyPacket(player)
                    continue
                }

                if (entity.viewers.contains(player.uniqueId)) {
                    continue
                }

                entity.viewers.add(player.uniqueId)
                entity.sendCreatePacket(player)
            }

        }

    }
}