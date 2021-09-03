package cc.fyre.entity.type.npc.listener

import cc.fyre.entity.event.NPCNameTagStateEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

object NPCVisibilityListener: Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onStateChange(event: NPCNameTagStateEvent) {
        event.npc.sendToAll{event.npc.ensureTagVisibility(it)}
    }

}