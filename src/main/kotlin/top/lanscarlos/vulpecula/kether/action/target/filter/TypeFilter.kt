package top.lanscarlos.vulpecula.kether.action.target.filter

import org.bukkit.entity.Entity
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.action.target.ActionTarget
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.readString

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.target.filter
 *
 * @author Lanscarlos
 * @since 2022-11-18 10:58
 */
object TypeFilter : ActionTarget.Reader {

    override val name: Array<String> = arrayOf("type")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionTarget.Handler {
        val source = reader.source(isRoot)
        val types = mutableSetOf<LiveData<String>>()
        if (reader.hasNextToken("[")) {
            while (!reader.hasNextToken("]")) {
                types += reader.readString()
            }
        } else {
            types += reader.readString()
        }

        return acceptHandler(source) { collection ->
            val exclude = mutableSetOf<String>()
            val include = mutableSetOf<String>()

            for (live in types) {
                val type = live.getOrNull(this) ?: continue
                if (type.startsWith('!')) {
                    // 排除类型
                    exclude += type.substring(1).uppercase()
                } else {
                    // 包含类型
                    include += type.uppercase()
                }
            }

            val iterator = collection.iterator()
            while (iterator.hasNext()) {
                val it = iterator.next()
                if (exclude.isNotEmpty()) {
                    if (it !is Entity || it.type.name in exclude) iterator.remove()
                }
                if (include.isNotEmpty()) {
                    if (it !is Entity || it.type.name !in include) iterator.remove()
                }
            }

            collection
        }
    }
}