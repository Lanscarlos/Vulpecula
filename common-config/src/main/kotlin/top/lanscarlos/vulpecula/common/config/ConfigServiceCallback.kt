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

    fun onFileDeleted(context: ConfigLoadContext, id: String, file: File)

    fun onFileCreated(context: ConfigLoadContext, id: String, file: File)

    fun onFileModified(context: ConfigLoadContext, id: String, file: File)

    /**
     * 当路径不存在时调用
     * */
    fun onLoadInit(context: ConfigLoadContext, directory: File)

    fun onLoadStarted(context: ConfigLoadContext) {}

    fun onLoadCompleted(context: ConfigLoadContext, time: Double)

    fun onLoadFailed(context: ConfigLoadContext, id: String, file: File, e: Throwable)

}