package cc.fyre.entity.util.moshi

import cc.fyre.entity.util.moshi.adapter.ItemStackJsonAdapter
import cc.fyre.entity.util.moshi.adapter.ListJsonAdapter
import cc.fyre.entity.util.moshi.adapter.LocationJsonAdapter
import cc.fyre.entity.util.moshi.adapter.UUIDJsonAdapter
import com.squareup.moshi.Moshi

import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.ParameterizedType

object MoshiUtil {

    private val builder: Moshi.Builder = Moshi.Builder()
        .add(UUIDJsonAdapter)
        .add(ListJsonAdapter)
        .add(LocationJsonAdapter)
        .add(ItemStackJsonAdapter)
        .addLast(KotlinJsonAdapterFactory())

    var instance: Moshi = this.builder
        .build()

    fun rebuild(use: (Moshi.Builder) -> Unit) {
        use.invoke(this.builder)

        this.instance = this.builder.build()
    }

    val LIST_STRING_TYPE: ParameterizedType = Types.newParameterizedType(List::class.java,String::class.java)
    val MAP_STRING_TO_ANY_TYPE: ParameterizedType = Types.newParameterizedType(Map::class.java,String::class.java,Any::class.java)

    const val PRETTY_PRINT_INDENT = "    "
}