package cc.fyre.entity

import java.util.function.Predicate
import org.bukkit.Bukkit

enum class EntityVisibility(val action: Predicate<Entity>) {

    HIDDEN(Predicate<Entity>{
        it.sendToAll{player -> it.sendDestroyPacket(player)}
        it.viewers.clear()
        return@Predicate true
    }),

    VISIBLE(Predicate<Entity>{
        Bukkit.getServer().onlinePlayers.forEach{player ->

            if (it.location.world != player.world) {
                return@forEach
            }

            if (it.getDistanceSquared(player.location) > Entity.DISTANCE) {
                return@forEach
            }

            it.viewers.add(player.uniqueId)
            it.sendCreatePacket(player)
        }
        return@Predicate true
    }),

}