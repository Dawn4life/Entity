package cc.fyre.entity

import cc.fyre.entity.type.hologram.adapter.HologramAdapter
import cc.fyre.entity.type.npc.NPC
import cc.fyre.entity.type.npc.listener.NPCVisibilityListener
import cc.fyre.entity.listener.EntityPacketListener

import cc.fyre.entity.listener.EntityVisibilityListener
import cc.fyre.entity.util.moshi.MoshiUtil
import cc.fyre.entity.thread.EntityThread
import cc.fyre.entity.type.hologram.Hologram
import cc.fyre.entity.type.hologram.line.HologramLine
import cc.fyre.entity.type.hologram.line.type.HologramItemLine
import cc.fyre.entity.type.hologram.line.type.HologramTextLine
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

import java.math.BigDecimal

object EntityHandler {

    private var count = Integer.MAX_VALUE

    private val entities = mutableMapOf<Int, Entity>()
    private val entitiesByName = mutableMapOf<String, Entity>()

    private val adapters = mutableListOf<HologramAdapter>()

    lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin

        MoshiUtil.rebuild{
            it.add(PolymorphicJsonAdapterFactory.of(Entity::class.java,"type")
                .withSubtype(NPC::class.java,"NPC")
                .withSubtype(Hologram::class.java,"HOLOGRAM")
            )
            it.add(
                PolymorphicJsonAdapterFactory.of(HologramLine::class.java,"type")
                .withSubtype(HologramTextLine::class.java,"TEXT")
                .withSubtype(HologramItemLine::class.java,"ITEM")
            )
        }

        EntityRepository.onLoad(plugin)
        EntityRepository.findAll().forEach{entity ->

            if (entity is NPC) {
                entity.hologram.parent = entity.id
            }

            this.register(entity)
        }

        listOf(
            NPCVisibilityListener,
            EntityPacketListener,
            EntityVisibilityListener
        ).forEach{Bukkit.getServer().pluginManager.registerEvents(it,plugin)}

        EntityThread.start()

        plugin.logger.info("[Entities] Loaded ${this.entities.size} ${if (this.entities.size == 1) "entity" else "entities"} to memory from ${EntityRepository.container.nameWithoutExtension} container.")
    }


    fun register(entity: Entity) {
        this.entities[entity.id] = entity
        this.entitiesByName[entity.name.lowercase()] = entity

        if (entity.file.exists()) {
            return
        }

        entity.file.createNewFile()

        EntityRepository.updateById(entity)
    }

    fun getAllEntities():List<Entity> {
        return this.entities.values.toList()
    }

    fun getEntityById(id: Int): Entity? {
        return this.entities[id]
    }

    fun getEntityByName(name: String): Entity? {
        return this.entitiesByName[name.lowercase()]
    }

    fun addAdapter(adapter: HologramAdapter) {
        this.adapters.add(adapter)
    }

    fun getAdapters():List<HologramAdapter> {
        return this.adapters
    }

    @JvmStatic
    fun getNewEntityId():Int {
        return this.count--
    }

    fun onDisable(plugin: JavaPlugin) {

        val written = this.entities.values.filter{it.persistent}.sumOf{

            if (!it.file.exists()) {
                return@sumOf BigDecimal.ZERO
            }

            try {
                EntityRepository.updateById(it)
            } catch (ex: Exception) {
                ex.printStackTrace()
                return@sumOf BigDecimal.ZERO
            }

            return@sumOf BigDecimal.ONE
        }.toInt()

        plugin.logger.info("[Entities] Wrote $written ${if (written == 1) "entity" else "entities"} to disk in ${EntityRepository.container.nameWithoutExtension} container.")
    }

}