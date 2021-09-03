package cc.fyre.entity.util.moshi.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.bukkit.Bukkit
import org.bukkit.Location

object LocationJsonAdapter {

    @ToJson
    fun toJson(location: Location):Map<@JvmSuppressWildcards String,@JvmSuppressWildcards Any?> {
        return mapOf(
            "x" to location.x,
            "y" to location.y,
            "z" to location.z,
            "yaw" to location.yaw,
            "pitch" to location.pitch,
            "world" to location.world.name
        )
    }

    @FromJson
    fun fromJson(json : Map<@JvmSuppressWildcards String,@JvmSuppressWildcards Any?>):Location {
        return Location(
            Bukkit.getServer().getWorld(json["world"] as String),
            json["x"] as Double,
            json["y"] as Double,
            json["z"] as Double,
            (json["yaw"] as Double).toFloat(),
            (json["pitch"] as Double).toFloat(),
        )
    }

}