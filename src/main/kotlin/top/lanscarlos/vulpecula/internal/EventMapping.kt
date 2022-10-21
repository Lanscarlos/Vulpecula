package top.lanscarlos.vulpecula.internal

import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.utils.*
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-08-21 15:45
 */
object EventMapping {

    private val folder by lazy {
        File(getDataFolder(), "listen-mapping.yml")
    }

    private val cache = mutableMapOf<String, String>()

    fun mapping(id: String): String? {
        return if (id.contains('.')) {
            id
        } else {
            cache[id]
        }
    }

    fun onFileChanged(file: File) {
        try {
            val start = timing()

            // 清除缓存
            cache.clear()

            // 加载文件
            file.toConfig().forEachString { key, value ->
                cache[key] = value
            }

            console().sendLang("Mapping-Load-Automatic-Succeeded", file.name, cache.size, timing(start))
        } catch (e: Exception) {
            e.printStackTrace()
            console().sendLang("Mapping-Load-Automatic-Failed", file.name, e.localizedMessage)
        }
    }

    fun load(): String {
        return try {
            val start = timing()

            folder.ifNotExists {
                releaseResourceFile("listen-mapping.yml")
            }.toConfig().forEachString { key, value ->
                cache[key] = value
            }

            // 添加监听器
            folder.addWatcher { onFileChanged(this) }

            console().asLangText("Mapping-Load-Succeeded", cache.size, timing(start)).also {
                console().sendMessage(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            console().asLangText("Mapping-Load-Failed", e.localizedMessage).also {
                console().sendMessage(it)
            }
        }
    }

}