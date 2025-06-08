package top.lanscarlos.vulpecula.common.config

import taboolib.common.io.digest
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common5.Coerce
import top.lanscarlos.vulpecula.common.core.utils.asLang
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
    fun load(sender: ProxyCommandSender = console()) {
        // 调试计时
        val startTime = System.nanoTime()
        try {
            if (!directory.exists()) {
                callback.onLoadInit(sender, directory)
            }

            // 重载开始
            callback.onLoadStarted(sender)

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

            var created = 0
            var modified = 0
            var deleted = 0
            var failed = 0


            // 处理被移除的文件
            for (file in cacheFiles - loadedFiles) {
                try {
                    callback.onFileDeleted(sender, buildFileId(file), file)
                    cache.remove(file)
                    hash.remove(file)
                    deleted += 1
                } catch (e: Exception) {
                    failed += 1
                    callback.onFileException(sender, buildFileId(file), file, e)
                }
            }

            // 处理新增的文件
            for (file in loadedFiles - cacheFiles) {
                try {
                    callback.onFileCreated(sender, buildFileId(file), file)
                    cache += file
                    hash[file] = file.digest("SHA-256")
                    created += 1
                } catch (e: Exception) {
                    failed += 1
                    callback.onFileException(sender, buildFileId(file), file, e)
                }
            }

            // 处理变动的文件
            for (file in loadedFiles intersect cacheFiles) {
                // 计算哈希指纹
                val hash = file.digest("SHA-256")
                // 哈希指纹比对
                if (hash == this.hash[file]) {
                    continue
                }
                try {
                    callback.onFileModified(sender, buildFileId(file), file)
                    this.hash[file] = hash
                    modified += 1
                } catch (e: Exception) {
                    failed += 1
                    callback.onFileException(sender, buildFileId(file), file, e)
                }
            }

            // 重载完成
            callback.onLoadSuccess(sender, created, modified, deleted, failed, timing(startTime))
        } catch (e: Throwable) {
            // 加载失败, 重置缓存
            reset()

            // 计算耗时, 单位毫秒
            callback.onLoadFailure(sender, timing(startTime), e)
        }
    }

    private fun buildDetailMessage(created: Int, modified: Int, deleted: Int, failed: Int): String {
        val builder = StringBuilder()
        if (created > 0) {
            builder.append(asLang("common-config-service-load-detail-created", created))
        }
        if (modified > 0) {
            if (builder.isNotEmpty()) {
                builder.append("; ")
            }
            builder.append(asLang("common-config-service-load-detail-modified", modified))
        }
        if (deleted > 0) {
            if (builder.isNotEmpty()) {
                builder.append("; ")
            }
            builder.append(asLang("common-config-service-load-detail-deleted", deleted))
        }
        if (failed > 0) {
            if (builder.isNotEmpty()) {
                builder.append("; ")
                builder.append(asLang("common-config-service-load-detail-failed", failed))
            }
        }
        return builder.toString()
    }

    private fun timing(time: Long): Double {
        return Coerce.format((System.nanoTime() - time).div(1000000.0))
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