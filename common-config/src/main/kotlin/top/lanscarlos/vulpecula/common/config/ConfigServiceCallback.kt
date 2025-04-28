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

    /**
     * 当路径不存在时调用
     * */
    fun onLoadInit(directory: File)

    fun onLoadStarted() {}

    fun onLoadCompleted(time: Double): String

    fun onLoadFailed(e: Throwable): String

}