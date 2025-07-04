package top.lanscarlos.vulpecula.common.config

import taboolib.common.io.digest
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common5.Coerce
import taboolib.common5.FileWatcher
import taboolib.module.configuration.Configuration
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
     * 被监听变动的文件
     * */
    val watched: HashSet<File> = hashSetOf()

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

            var created = 0
            var modified = 0
            var deleted = 0
            var failed = 0
            val cacheFiles = HashSet(cache)
            cache.clear()
            for (file in directory.walk()) {
                if (file.isDirectory) {
                    continue
                }
                if (file.name[0] == '#') {
                    continue
                }

                if (file in cacheFiles) {
                    // 计算哈希指纹
                    val hash = file.digest("SHA-256")
                    // 哈希指纹比对
                    if (hash == this.hash[file]) {
                        continue
                    }
                    try {
                        callback.onFileModified(sender, buildFileId(file), file)
                        this.hash[file] = hash
                        detectAutoReload(file)
                        modified += 1
                    } catch (e: Exception) {
                        failed += 1
                        callback.onFileException(sender, buildFileId(file), file, e)
                    } finally {
                        cacheFiles.remove(file)
                    }
                } else {
                    // 新增的文件
                    try {
                        callback.onFileCreated(sender, buildFileId(file), file)
                        cache += file
                        hash[file] = file.digest("SHA-256")
                        detectAutoReload(file)
                        created += 1
                    } catch (e: Exception) {
                        failed += 1
                        callback.onFileException(sender, buildFileId(file), file, e)
                    }
                }
            }

            // 处理剩余被删除的文件
            for (file in cacheFiles) {
                try {
                    callback.onFileDeleted(sender, buildFileId(file), file)
                    cache.remove(file)
                    hash.remove(file)
                    deleted += 1
                } catch (e: Exception) {
                    failed += 1
                    callback.onFileException(sender, buildFileId(file), file, e)
                } finally {
                    removeFileWatcher(file)
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

    private fun detectAutoReload(file: File) {
        if (file.extension != "yml" && file.extension != "yaml") {
            return
        }
        val config = Configuration.loadFromFile(file)
        if (!config.getBoolean("debug.auto-reload", false)) {
            // 未启用自动重载
            return
        }
        addFileWatcher(file)
    }

    private fun addFileWatcher(file: File) {
        FileWatcher.INSTANCE.addSimpleListener(file, ::onFileModified, false)
        watched.add(file)
    }

    private fun removeFileWatcher(file: File) {
        if (!watched.remove(file)) {
            return
        }
        FileWatcher.INSTANCE.removeListener(file)
    }

    private fun onFileModified(file: File) {
        // 调试计时
        val startTime = System.nanoTime()
        // 计算哈希指纹
        val hash = file.digest("SHA-256")
        // 哈希指纹比对
        if (hash == this.hash[file]) {
            return
        }
        val id = buildFileId(file)
        try {
            callback.onFileModified(console(), id, file)
            callback.onLoadAutomatic(console(), id, file, timing(startTime))
            this.hash[file] = hash
        } catch (e: Exception) {
            callback.onFileException(console(), id, file, e)
        }
    }

    private fun timing(time: Long): Double {
        return Coerce.format((System.nanoTime() - time).div(1000000.0))
    }

    /**
     * 根据文件相对路径获取文件的 Id
     * 例如： ./Vulpecula/script/example/default.yml -> example.default
     * */
    private fun buildFileId(file: File): String {
        val rootPath = directory.toURI().normalize()
        val targetPath = file.toURI().normalize()
        val relativePath = rootPath.relativize(targetPath).path
        return relativePath.toString().replace(File.separatorChar, '.').substringBeforeLast('.')
    }

}