package cc.fyre.entity.type.hologram.adapter

import org.bukkit.entity.Player

interface HologramAdapter {
    
    fun resolve(player: Player): HashMap<String,Any>
    
}