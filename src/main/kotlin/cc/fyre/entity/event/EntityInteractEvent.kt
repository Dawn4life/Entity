package cc.fyre.entity.event

import cc.fyre.entity.Entity
import cc.fyre.entity.type.npc.NPC
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class EntityInteractEvent(val entity: Entity,val player: Player,val action: EntityInteractAction): Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {

        @JvmStatic private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }

    }

    enum class EntityInteractAction {
        RIGHT_CLICK,
        LEFT_CLICK
    }

}