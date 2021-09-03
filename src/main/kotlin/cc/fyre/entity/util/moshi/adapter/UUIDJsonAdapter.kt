package cc.fyre.entity.util.moshi.adapter

import java.util.*
import com.squareup.moshi.*

object UUIDJsonAdapter {

    @ToJson
    fun toJson(uuid: UUID):String {
        return uuid.toString()
    }

    @FromJson
    fun fromJson(json: String):UUID {
        return UUID.fromString(json)
    }

}