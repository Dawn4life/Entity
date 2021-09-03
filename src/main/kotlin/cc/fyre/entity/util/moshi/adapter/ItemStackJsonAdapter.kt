package cc.fyre.entity.util.moshi.adapter

import cc.fyre.entity.util.moshi.MoshiUtil
import cc.fyre.entity.util.moshi.json.JsonObject
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*

object ItemStackJsonAdapter {

    @ToJson
    fun toJson(item: ItemStack):Map<@JvmSuppressWildcards String,@JvmSuppressWildcards Any?> {

        val toReturn = mutableMapOf<String,Any>()

        toReturn["type"] = item.type.name
        toReturn["amount"] = item.amount
        toReturn["durability"] = item.durability

        val itemMeta = item.itemMeta
        val metadata = mutableMapOf<String,Any>()

        if (itemMeta.hasLore()) {
            metadata["lore"] = MoshiUtil.instance.adapter<List<String>>(MoshiUtil.LIST_STRING_TYPE).toJson(itemMeta.lore)
        }

        if (itemMeta.hasEnchants()) {
            metadata["enchants"] = MoshiUtil.instance.adapter<Map<String,Any>>(MoshiUtil.MAP_STRING_TO_ANY_TYPE).toJson(itemMeta.enchants.mapKeys{it.key.name})
        }

        if (itemMeta.hasDisplayName()) {
            metadata["displayName"] = itemMeta.displayName
        }

        if (item.hasItemMeta()) {
            metadata["metadata"] = item.itemMeta.serialize()
        }

        when (itemMeta) {
            is MapMeta -> {
                metadata["scaling"] = itemMeta.isScaling
            }
            is BookMeta -> {

                if (itemMeta.hasTitle()) {
                    metadata["title"] = itemMeta.title
                }

                if (itemMeta.hasPages()) {
                    metadata["pages"] = MoshiUtil.instance.adapter<List<String>>(MoshiUtil.LIST_STRING_TYPE).toJson(itemMeta.pages)
                }

                if (itemMeta.hasAuthor()) {
                    metadata["author"] = itemMeta.author
                }

            }
            is SkullMeta -> {

                if (itemMeta.hasOwner()) {
                    metadata["owner"] = itemMeta.owner
                }

            }
            is PotionMeta -> {
                //TODO
            }
            is FireworkMeta -> {
                //TODO
            }
            is FireworkEffectMeta -> {

                if (itemMeta.hasEffect()) {
                    metadata["effect"] = MoshiUtil.instance.adapter(FireworkEffect::class.java).toJson(itemMeta.effect)
                }

            }
            is LeatherArmorMeta -> {
                metadata["color"] = itemMeta.color.asRGB()
            }
        }

        toReturn["metadata"] = metadata

        return toReturn
    }

    @FromJson
    fun fromJson(json : Map<@JvmSuppressWildcards String,@JvmSuppressWildcards Any?>):ItemStack {

        val toReturn = ItemStack(Material.valueOf(json["type"] as String))

        toReturn.amount = (json["amount"] as Double).toInt()
        toReturn.durability = (json["durability"] as Double).toInt().toShort()

        val itemMeta = toReturn.itemMeta
        val metadata = JsonObject(json["metadata"] as Map<*,*>)

        if (metadata.containsKey("displayName")) {
            itemMeta.displayName = metadata.getString("displayName")
        }

        if (metadata.containsKey("lore")) {
            itemMeta.lore = MoshiUtil.instance.adapter<List<String>>(MoshiUtil.LIST_STRING_TYPE).fromJson(metadata.getString("lore")!!)
        }

        when (itemMeta) {
            is MapMeta -> {
                itemMeta.isScaling = metadata.getBoolean("scaling") ?: false
            }
            is BookMeta -> {

                if (metadata.containsKey("title")) {
                    itemMeta.title = metadata.getString("title")
                }

                if (metadata.containsKey("pages")) {
                    itemMeta.pages =  MoshiUtil.instance.adapter<List<String>>(MoshiUtil.LIST_STRING_TYPE).fromJson(metadata.getString("pages")!!)
                }

                if (metadata.containsKey("author")) {
                    itemMeta.author = metadata.getString("author")!!
                }

            }
            is SkullMeta -> {

                if (metadata.containsKey("owner")) {
                    itemMeta.owner = metadata.getString("owner")
                }

            }
            is PotionMeta -> {
                //TODO
            }
            is FireworkMeta -> {
                //TODO
            }
            is FireworkEffectMeta -> {

                if (metadata.containsKey("effect")) {
                    itemMeta.effect = MoshiUtil.instance.adapter(FireworkEffect::class.java).fromJson(metadata.getString("effect"))
                }

            }

            is LeatherArmorMeta -> {
                itemMeta.color = Color.fromRGB(metadata.getInt("color")!!)
            }
        }


        // Enchants always last
        if (metadata.containsKey("enchants")) {
            MoshiUtil.instance.adapter<Map<String,Any>>(MoshiUtil.MAP_STRING_TO_ANY_TYPE).fromJson(metadata.getString("enchants")!!)!!.entries.associate{
                Enchantment.getByName(it.key) to (it.value as Double).toInt()
            }.forEach{toReturn.addUnsafeEnchantment(it.key,it.value)}
        }

        toReturn.itemMeta = itemMeta

        return toReturn
    }


}