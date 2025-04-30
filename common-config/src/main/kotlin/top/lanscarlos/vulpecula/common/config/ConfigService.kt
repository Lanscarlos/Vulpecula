package top.lanscarlos.vulpecula.common.config

import taboolib.common.io.digest
import taboolib.common.platform.function.getDataFolder
import taboolib.common5.Coerce
import java.io.File
import java.util.*
import kotlin.collections.HashSet

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * 配置服务
 *
 * @author Lanscarlos
 * @since 2025/4/25 11:11
 */
class ConfigService(val id: String, val directory: File, val priority: Int, val callback: ConfigServiceCallback) {

    /**
     * 相对路径
     * */
    val path = getDataFolder().toPath().normalize().relativize(directory.toPath().normalize()).toString()

    /**
     * 文件缓存
     * */
    val cache: HashSet<File> = hashSetOf()

    /**
     * 文件哈希指纹
     * */
    val hash = HashMap<File, String>()

    /**
     * 重置缓存
     * */
    fun reset() {
        cache.clear()
        hash.clear()
    }

    /**
     * 加载配置
     * */
    fun load(): String {
        try {
            // 调试计时
            val startTime = System.nanoTime()

            if (!directory.exists()) {
                callback.onLoadInit(directory)
            }

            // 重载开始
            callback.onLoadStarted()

            // 获取所有文件
            val queue = LinkedList<File>()
            val loadedFiles = hashSetOf<File>()
            queue += directory
            while (queue.isNotEmpty()) {
                val file = queue.poll()
                if (file.isFile) {
                    if (!file.exists() || file.name.first() == '#') {
                        // 排除不存在或被注释的文件
                        continue
                    }
                    loadedFiles += file
                    continue
                }
                val files = file.listFiles()?.filter { it.name.first() != '#' } ?: continue
                queue.addAll(files)
            }
            val cacheFiles = HashSet(cache)

            // 处理被移除的文件
            for (file in cacheFiles - loadedFiles) {
                cache.remove(file)
                callback.onFileDeleted(buildFileId(file), file)
            }

            // 处理新增的文件
            for (file in loadedFiles - cacheFiles) {
                cache += file
                callback.onFileCreated(buildFileId(file), file)
            }

            // 处理变动的文件
            for (file in loadedFiles intersect cacheFiles) {
                // 计算哈希指纹
                val hash = file.digest("SHA-256")
                // 哈希指纹比对
                if (hash == this.hash[file]) {
                    continue
                }
                callback.onFileModified(buildFileId(file), file)
                this.hash[file] = hash
            }

            // 计算耗时, 单位毫秒
            val time = Coerce.format((System.nanoTime() - startTime).div(1000000.0))
            // 重载完成
            return callback.onLoadCompleted(time)
        } catch (e: Throwable) {
            // 重载失败, 重置缓存
            reset()
            return callback.onLoadFailed(e)
        }
    }

    /**
     * 根据文件相对路径获取文件的 Id
     * 例如： ./Vulpecula/script/example/default.yml -> example.default
     * */
    private fun buildFileId(file: File): String {
        val rootPath = directory.toPath().normalize()
        val targetPath = file.toPath().normalize()
        val relativePath = rootPath.relativize(targetPath)
        return relativePath.toString().replace(File.separatorChar, '.').substringBeforeLast('.')
    }

}