package cc.fyre.entity.util.moshi

import com.squareup.moshi.JsonAdapter

fun <T> JsonAdapter<T>.setPrettyPrinting():JsonAdapter<T> {
    return this.indent(MoshiUtil.PRETTY_PRINT_INDENT)
}