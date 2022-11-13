package top.lanscarlos.vulpecula.internal

import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.internal.compiler.ScriptCompiler
import top.lanscarlos.vulpecula.utils.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-08-22 14:28
 */
object ScriptFragment {

    private val folder by lazy {
        File(getDataFolder(), "fragments")
    }

    private val mapping = ConcurrentHashMap<String, File>() // id -> File
    private val linked = ConcurrentHashMap<String, ScriptCompiler>()
    private val cache = ConcurrentHashMap<String, String>()

    fun get(id: String): String? {
        return cache[id]
    }

    /**
     * 连接片段
     * */
    fun link(compiler: ScriptCompiler, id: String): String? {
        if (!cache.contains(id)) return null
        linked[id] = compiler
        return cache[id]
    }

    /**
     * 断开连接
     * */
    fun unlink(compiler: ScriptCompiler) {
        linked.filterValues { it == compiler }.keys.forEach {
            linked.remove(it)
        }
    }

    private fun onFileChanged(file: File) {
        try {
            val start = timing()

            // 删除旧节点
            mapping.filterValues { it == file }.forEach {
                mapping.remove(it.key)
                cache.remove(it.key)
            }

            // 记录数量
            val size = cache.size

            // 加载文件
            file.toConfig().forEachLine { key, value ->
                cache[key] = value
                mapping[key] = file
            }

            console().sendLang("Fragment-Load-Automatic-Succeeded", file.name, (cache.size - size), timing(start))
        } catch (e: Exception) {
            console().sendLang("Fragment-Load-Automatic-Failed", file.name, e.localizedMessage)
        }
    }

    fun load(): String {
        return try {
            // 耗时检测
            val start = timing()

            // 移除原有映射
            mapping.values.forEach {
                it.removeWatcher()
            }
            mapping.clear()

            // 清空缓存
            cache.clear()

            folder.ifNotExists {
                listOf(
//                        "#example.yml",
                    "def.yml"
                ).forEach { releaseResourceFile("fragments/$it", true) }
            }.getFiles().forEach {

                // 添加文件监听
                it.addWatcher { onFileChanged(this) }

                // 加载文件
                it.toConfig().forEachLine { key, value ->
                    cache[key] = value
                    mapping[key] = it
                }
            }

            console().asLangText("Fragment-Load-Succeeded", cache.size, timing(start)).also {
                console().sendMessage(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            console().asLangText("Fragment-Load-Failed", e.localizedMessage).also {
                console().sendMessage(it)
            }
        }
    }

}