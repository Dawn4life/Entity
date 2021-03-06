package cc.fyre.entity.event

import cc.fyre.entity.type.npc.NPC
import cc.fyre.entity.EntityVisibility
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class NPCNameTagStateEvent(val npc: NPC, val new: EntityVisibility) : Event() {

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

}