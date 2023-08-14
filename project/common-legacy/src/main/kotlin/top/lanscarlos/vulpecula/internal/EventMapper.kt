package top.lanscarlos.vulpecula.internal

import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common5.cbool
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.config.DynamicConfig.Companion.bindConfigNode
import top.lanscarlos.vulpecula.utils.*
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-08-21 15:45
 */
object EventMapper {

    val automaticReload by bindConfigNode("automatic-reload.listen-mapping") {
        it?.cbool ?: false
    }

    private val folder by lazy {
        File(getDataFolder(), "listen-mapping.yml")
    }

    val cache = mutableMapOf<String, String>()
    val linked = mutableMapOf<String, MutableSet<EventListener>>()

    /**
     * 获取事件类
     * */
    fun mapping(listener: EventListener): Class<*>? {
        val eventName = if (listener.mapper.contains('.')) {
            listener.mapper
        } else {
            linked.computeIfAbsent(listener.mapper) { mutableSetOf() } += listener
            cache[listener.mapper] ?: let {
                console().sendLang("Event-Mapping-Failed-Undefined", listener.mapper)
                return null
            }
        }

        return try {
            Class.forName(eventName)
        } catch (ignored: Exception) {
            console().sendLang("Event-Mapping-Failed-Class-Not-Found", eventName)
            null
        }
    }

    fun onFileChanged(file: File) {
        if (!automaticReload) {
            file.removeWatcher()
            return
        }

        val start = timing()
        try {

            val config = file.toConfig()
            val keys = config.getKeys(false).toMutableSet()

            // 遍历已存在的映射
            val iterator = cache.iterator()
            while (iterator.hasNext()) {
                val mapper = iterator.next()
                if (mapper.key in keys) {
                    // 当前映射仍然存在与配置中
                    val newValue = config.getString(mapper.key) ?: "MAPPING_UNDEFINED"
                    if (mapper.value != newValue) {
                        // 更新数据
                        cache[mapper.key] = newValue
                        // 数据发生变化，通知相关调度器重新注册事件监听器
                        linked[mapper.key]?.forEach { it.registerListener() }
                    }

                    // 移除该 id
                    keys -= mapper.key
                } else {
                    // 当前映射已被删除，通知相关调度器重新注册事件监听器（抛出异常）
                    linked.remove(mapper.key)?.let { set ->
                        set.forEach { it.registerListener() }
                        set.clear()
                    }

                    // 移出缓存
                    iterator.remove()
                }
            }

            // 遍历新的映射
            for (key in keys) {
                config.getString(key)?.let { mapping ->
                    cache[key] = mapping
                    linked[key]?.forEach { it.registerListener() }
                }
            }

            console().sendLang("Event-Mapping-Load-Automatic-Succeeded", file.name, cache.size, timing(start))
        } catch (e: Exception) {
            e.printStackTrace()
            console().sendLang("Event-Mapping-Load-Automatic-Failed", file.name, e.localizedMessage, timing(start))
        }
    }

    fun load(): String {
        return try {
            val start = timing()

            folder.ifNotExists {
                releaseResourceFile("listen-mapping.yml")
            }.toConfig().forEachLine { key, value ->
                cache[key] = value
            }

            // 添加监听器
            if (automaticReload) {
                folder.addWatcher { onFileChanged(this) }
            }

            console().asLangText("Event-Mapping-Load-Succeeded", cache.size, timing(start)).also {
                console().sendMessage(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            console().asLangText("Event-Mapping-Load-Failed", e.localizedMessage).also {
                console().sendMessage(it)
            }
        }
    }

}