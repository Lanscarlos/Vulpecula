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

    fun onFileCreated(sender: ProxyCommandSender, id: String, file: File)

    fun onFileModified(sender: ProxyCommandSender, id: String, file: File)

    fun onFileDeleted(sender: ProxyCommandSender, id: String, file: File)

    fun onFileException(sender: ProxyCommandSender, id: String, file: File, e: Throwable)

    /**
     * 当路径不存在时调用
     * */
    fun onLoadInit(sender: ProxyCommandSender, directory: File)

    fun onLoadStarted(sender: ProxyCommandSender) {}

    fun onLoadAutomatic(sender: ProxyCommandSender, id: String, file: File, time: Double)

    /**
     * 加载成功时调用
     *
     * @param statistics 统计数据
     * */
    fun onLoadSuccess(sender: ProxyCommandSender, statistics: ConfigStatistics)

    fun onLoadFailure(sender: ProxyCommandSender, e: Throwable) {}

}