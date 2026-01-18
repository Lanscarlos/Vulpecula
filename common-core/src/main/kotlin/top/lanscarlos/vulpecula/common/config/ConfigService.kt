package top.lanscarlos.vulpecula.common.config

import taboolib.common.io.digest
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.common5.Coerce
import taboolib.common5.FileWatcher
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.common.utils.TimeUtil
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
class ConfigService(val id: String, val name: String, val directory: File, val priority: Int, val callback: ConfigServiceCallback) {

    /**
     * 相对路径
     * */
//    val path = getDataFolder().toPath().normalize().relativize(directory.toPath().normalize()).toString()

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
    val watched: HashSet<String> = hashSetOf()

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
        // 创建统计数据
        val statistics = ConfigStatistics(TimeUtil.startTiming())
        try {
            if (!directory.exists()) {
                callback.onLoadInit(sender, directory)
            }

            // 重载开始
            callback.onLoadStarted(sender)

            val cacheFiles = HashSet(cache)
            cache.clear()
            for (file in directory.walk()) {
                if (file.isDirectory) {
                    continue
                }
                if (file.name[0] == '#') {
                    continue
                }

                statistics.scannedFiles += file

                if (file !in cacheFiles) {
                    // 新增的文件
                    try {
                        callback.onFileCreated(sender, getFileId(file), file)
                        cache += file
                        hash[file] = file.digest("SHA-256")
                        detectAutoReload(sender, file)
                        statistics.createdFiles += file
                    } catch (e: Throwable) {
                        statistics.failedFiles += file
                        callback.onFileException(sender, getFileId(file), file, e)
                    }
                    continue
                }

                // 计算哈希指纹并进行比对
                val hash = file.digest("SHA-256")
                if (hash == this.hash[file]) {
                    // 文件无修改
                    cacheFiles.remove(file)
                    cache += file
                    statistics.unmodifiedFiles += file
                    continue
                }

                // 文件修改
                try {
                    callback.onFileModified(sender, getFileId(file), file)
                    cache += file
                    this.hash[file] = hash
                    detectAutoReload(sender, file)
                    statistics.modifiedFiles += file
                } catch (e: Throwable) {
                    statistics.failedFiles += file
                    callback.onFileException(sender, getFileId(file), file, e)
                } finally {
                    cacheFiles.remove(file)
                }
            }

            // 处理剩余被删除的文件
            for (file in cacheFiles) {
                try {
                    callback.onFileDeleted(sender, getFileId(file), file)
                    hash.remove(file)
                    statistics.deletedFiles += file
                } catch (e: Throwable) {
                    statistics.failedFiles += file
                    callback.onFileException(sender, getFileId(file), file, e)
                } finally {
                    removeFileWatcher(sender, file)
                }
            }

            // 重载完成
            callback.onLoadSuccess(sender, statistics)
            onDisplayStatistics(sender, statistics)
        } catch (e: Throwable) {
            // 加载失败, 重置缓存
            reset()

            // 计算耗时, 单位毫秒
            callback.onLoadFailure(sender, e)

            Lang.COMMON_CONFIG_SERVICE_LOAD_FAILURE.error(sender, name, e.localizedMessage)
            e.printStackTrace()
        }
    }

    private fun onDisplayStatistics(sender: ProxyCommandSender, statistics: ConfigStatistics) {
        if (statistics.scannedFiles.isNotEmpty()) {
            statistics.displayDetails += Lang.COMMON_CONFIG_LOAD_STATISTICS_SCANNED.asText(sender, statistics.scannedFiles.size)
        }
        if (statistics.unmodifiedFiles.isNotEmpty()) {
            statistics.displayDetails += Lang.COMMON_CONFIG_LOAD_STATISTICS_UNMODIFIED.asText(sender, statistics.unmodifiedFiles.size)
        }
        if (statistics.createdFiles.isNotEmpty()) {
            statistics.displayDetails += Lang.COMMON_CONFIG_LOAD_STATISTICS_CREATED.asText(sender, statistics.createdFiles.size)
        }
        if (statistics.modifiedFiles.isNotEmpty()) {
            statistics.displayDetails += Lang.COMMON_CONFIG_LOAD_STATISTICS_MODIFIED.asText(sender, statistics.modifiedFiles.size)
        }
        if (statistics.deletedFiles.isNotEmpty()) {
            statistics.displayDetails += Lang.COMMON_CONFIG_LOAD_STATISTICS_DELETED.asText(sender, statistics.deletedFiles.size)
        }
        if (statistics.failedFiles.isNotEmpty()) {
            statistics.displayDetails += Lang.COMMON_CONFIG_LOAD_STATISTICS_FAILED.asText(sender, statistics.failedFiles.size)
        }
        for ((index, content) in statistics.displayDetails.withIndex()) {
            if (index < statistics.displayDetails.size - 1) {
                Lang.COMMON_CONFIG_LOAD_STATISTICS_BRANCH.info(sender, content)
            } else {
                Lang.COMMON_CONFIG_LOAD_STATISTICS_BRANCH_END.info(sender, content)
            }
        }
    }

    private fun detectAutoReload(sender: ProxyCommandSender, file: File) {
        if (file.extension != "yml" && file.extension != "yaml") {
            return
        }
        val config = Configuration.loadFromFile(file)
        if (config.getBoolean("debug.auto-reload", false)) {
            // 启用自动重载
            addFileWatcher(sender, file)
        } else if (watched.contains(file.absolutePath)) {
            // 关闭自动加载
            removeFileWatcher(sender, file)
        }
    }

    private fun addFileWatcher(sender: ProxyCommandSender, file: File) {
        if (watched.contains(file.absolutePath)) {
            return
        }
        FileWatcher.INSTANCE.addSimpleListener(file, ::onFileModified, false)
        watched.add(file.absolutePath)
        val path = directory.toURI().normalize().relativize(file.toURI().normalize()).path
        Lang.COMMON_CONFIG_LOAD_AUTOMATIC_ENABLED.info(sender, path)
    }

    private fun removeFileWatcher(sender: ProxyCommandSender, file: File) {
        if (!watched.remove(file.absolutePath)) {
            return
        }
        FileWatcher.INSTANCE.removeListener(file)
        val path = directory.toURI().normalize().relativize(file.toURI().normalize()).path
        Lang.COMMON_CONFIG_LOAD_AUTOMATIC_DISABLED.info(sender, path)
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
        val id = getFileId(file)
        try {
            callback.onFileModified(console(), id, file)
            callback.onLoadAutomatic(console(), id, file, timing(startTime))
            this.hash[file] = hash
            detectAutoReload(console(), file)
        } catch (e: Exception) {
            e.printStackTrace()
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
    private fun getFileId(file: File): String {
        val rootPath = directory.toURI().normalize()
        val targetPath = file.toURI().normalize()
        val relativePath = rootPath.relativize(targetPath).path
        return relativePath.toString().replace(File.separatorChar, '.').substringBeforeLast('.')
    }

}