package top.lanscarlos.vulpecula.common.config

import taboolib.common.platform.ProxyCommandSender
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

    fun onFileDeleted(sender: ProxyCommandSender, id: String, file: File)

    fun onFileCreated(sender: ProxyCommandSender, id: String, file: File)

    fun onFileModified(sender: ProxyCommandSender, id: String, file: File)

    fun onFileException(sender: ProxyCommandSender, id: String, file: File, e: Exception)

    /**
     * 当路径不存在时调用
     * */
    fun onLoadInit(sender: ProxyCommandSender, directory: File)

    fun onLoadStarted(sender: ProxyCommandSender) {}

    fun onLoadSuccess(sender: ProxyCommandSender, time: Double)

    fun onLoadFailure(sender: ProxyCommandSender, time: Double, e: Throwable)

}