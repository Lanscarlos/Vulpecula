package top.lanscarlos.vulpecula.common.config

import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * 文件监听
 *
 * @author Lanscarlos
 * @since 2025/4/25 14:06
 */
interface ConfigServiceCallback {

    fun onFileDeleted(id: String, file: File)

    fun onFileCreated(id: String, file: File)

    fun onFileModified(id: String, file: File)

    fun onReloadStarted() {}

    fun onReloadCompleted(time: Double) {}

    fun onReloadFailed(e: Throwable) {
        e.printStackTrace()
    }

}