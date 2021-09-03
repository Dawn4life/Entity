package cc.fyre.entity.util.moshi.json

import cc.fyre.entity.util.moshi.MoshiUtil
import com.squareup.moshi.Moshi

class JsonObject {

    constructor() {
        this.entries = mutableMapOf()
    }

    constructor(entries: Map<*,*>) {

        val newEntries = mutableMapOf<String,Any>()

        for (entry in entries) {

            if (entry.key !is String || entry.value == null) {
                continue
            }

            newEntries[entry.key as String] = entry.value!!
        }

        this.entries = newEntries
    }

    private var entries: MutableMap<String,Any>

    fun get():Map<String,Any> {
        return this.entries
    }

    operator fun set(key: String,value: Any) {
        this.entries[key] = value
    }

    fun containsKey(key: String):Boolean {
        return this.entries.containsKey(key)
    }

    fun getInt(key: String):Int? {

        val value = this.entries[key]

        if (value is Double) {
            return value.toInt()
        }

        return value as? Int
    }

    fun getLong(key: String):Long? {

        val value = this.entries[key]

        if (value is Double) {
            return value.toLong()
        }

        return value as? Long
    }

    fun getFloat(key: String):Float? {
        return this.entries[key] as? Float
    }

    fun getString(key: String):String? {
        return this.entries[key] as? String
    }

    fun getDouble(key: String):Double? {
        return this.entries[key] as? Double
    }

    fun getBoolean(key: String):Boolean? {
        return this.entries[key] as? Boolean
    }

    fun getJsonObject(key: String):JsonObject {
        return JsonObject(this.entries[key] as? Map<*,*> ?: mapOf<String,Any>())
    }

    fun toJson(moshi: Moshi):String {
        return moshi.adapter<Map<String,Any>>(MoshiUtil.MAP_STRING_TO_ANY_TYPE).toJson(this.entries)
    }

}