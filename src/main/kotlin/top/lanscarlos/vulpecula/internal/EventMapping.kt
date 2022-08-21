package top.lanscarlos.vulpecula.internal

import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.lang.asLangText
import top.lanscarlos.vulpecula.utils.forEachString
import top.lanscarlos.vulpecula.utils.ifNotExists
import top.lanscarlos.vulpecula.utils.timing
import top.lanscarlos.vulpecula.utils.toConfig
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

    private val mapping = mutableMapOf<String, String>()

    fun mapping(id: String): String? {
        return if (id.contains('.')) {
            id
        } else {
            mapping[id]
        }
    }

    fun load(): String {
        return try {
            val start = timing()

            folder.ifNotExists {
                releaseResourceFile("listen-mapping.yml")
            }.toConfig().forEachString { key, value ->
                mapping[key] = value
            }

            console().asLangText("Mapping-Load-Succeeded", mapping.size, timing(start)).also {
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