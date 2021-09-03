package cc.fyre.entity

import cc.fyre.entity.util.moshi.MoshiUtil
import cc.fyre.entity.util.moshi.setPrettyPrinting
import com.squareup.moshi.JsonReader
import okhttp3.internal.io.FileSystem
import okio.buffer
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileWriter
import java.io.IOException

object EntityRepository {

    lateinit var container: File

    fun findAll():List<Entity> {
        return (this.container.listFiles() ?: arrayOf()).filterNotNull()
            .mapNotNull{JsonReader.of(FileSystem.SYSTEM.source(it).buffer())}
            .mapNotNull{MoshiUtil.instance.adapter(Entity::class.java).setPrettyPrinting().fromJson(it)}
    }

    fun onLoad(plugin: JavaPlugin) {
        this.container = File("${plugin.dataFolder.absolutePath}/entities")
        this.container.mkdir()
    }

    fun updateById(entity: Entity) {

        val writer = FileWriter(entity.file)

        try {
            writer.write(MoshiUtil.instance.adapter(Entity::class.java).setPrettyPrinting().toJson(entity))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return
        }

        writer.close()
    }

    fun deleteById(entity: Entity) {

        if (!entity.file.exists()) {
            return
        }

        entity.file.delete()
    }

}