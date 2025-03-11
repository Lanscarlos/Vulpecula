package top.lanscarlos.vulpecula.common.config

import taboolib.common.io.digest
import taboolib.common.platform.function.getDataFolder
import taboolib.common5.Coerce
import taboolib.module.configuration.Configuration
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-03-11 13:11
 */
class ConfigSubscriber(val file: File, val priority: Int, val callback: Callback) {

    interface Callback {

        fun onReloadStarted() {}

        fun onFileDeleted(file: File)

        fun onFileCreated(file: File, config: Configuration)

        fun onFileModified(file: File, config: Configuration)

        fun onReloadCompleted(time: Double) {}

        fun onReloadFailed(e: Throwable) {
            e.printStackTrace()
        }

    }

    /**
     * 相对路径
     * */
    val path = getDataFolder().toPath().normalize().relativize(file.toPath().normalize()).toString()

    /**
     * 配置缓存
     * */
    val cache: HashMap<File, Configuration> = hashMapOf()

    /**
     * 文件哈希指纹
     * */
    val hash = HashMap<File, String>()

    fun reload() {
        try {
            // 调试计时
            val startTime = System.nanoTime()

            // 重载开始
            callback.onReloadStarted()

            // 获取所有文件
            val queue = LinkedList<File>()
            val files = hashSetOf<File>()
            queue += file
            while (queue.isNotEmpty()) {
                val file = queue.poll()
                if (file.isFile) {
                    files += file
                    continue
                }
                queue.addAll(file.listFiles() ?: continue)
            }

            // 处理被移除的文件
            for (file in cache.keys.filter { it !in files }) {
                cache.remove(file)
                callback.onFileDeleted(file)
            }

            // 处理新增的文件
            for (file in files.filter { it !in cache }) {
                val config = Configuration.loadFromFile(file)
                cache[file] = config
                callback.onFileCreated(file, config)
            }

            // 处理变动的文件
            for (file in files.filter { it in cache }) {
                // 计算哈希指纹
                val hash = file.digest("SHA-256")
                // 哈希指纹比对
                if (hash == this.hash[file]) {
                    continue
                }
                val config = cache[file]!!
                config.reload()
                callback.onFileModified(file, config)
                this.hash[file] = hash
            }

            // 计算耗时, 单位毫秒
            val time = Coerce.format((System.nanoTime() - startTime).div(1000000.0))
            // 重载完成
            callback.onReloadCompleted(time)
        } catch (e: Throwable) {
            // 重载失败
            callback.onReloadFailed(e)
        }
    }

}